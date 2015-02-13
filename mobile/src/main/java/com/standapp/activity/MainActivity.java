package com.standapp.activity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.standapp.R;
import com.standapp.activity.common.StandAppBaseActionBarActivity;
import com.standapp.activity.error.ChromeExtErrorActivity;
import com.standapp.activity.error.GenericErrorActivity;
import com.standapp.backend.UserHelper;
import com.standapp.backend.UserInfoListener;
import com.standapp.backend.UserInfoMediator;
import com.standapp.fragment.OnFragmentCreatedListener;
import com.standapp.google.GooglePlayServicesHelper;
import com.standapp.google.gcm.GCMHelper;
import com.standapp.google.gcm.GCMHelperListener;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.logger.LogConstants;
import com.standapp.preferences.PreferenceAccess;
import com.standapp.util.User;
import com.standapp.util.UserInfo;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/***
 *
 * Auth flow:
 *
 * .onStart --> googleFitAPIHelper.connect(); -> onFailure (start resolutions) || onConnect (*success*)
 *
 * .onResume -> userHelper.openChooseAccountDialog(this); ->
 *              onActivityResult ->
 *              userHelper.refreshUser(userAccount) ->
 *              onUserRefreshed ->
 *              gcmHelper.init(this, user.get_id()); ->
 *              onRegisterSuccess/onAlreadyRegistered (*success*)
 *
 * .onFragmentCreated ->
             * userHelper.refreshUser(userAccount) ->
             * onUserRefreshed ->
             * gcmHelper.init(this, user.get_id()); ->
             * onRegisterSuccess/onAlreadyRegistered (*success*)
 *
 */
public class MainActivity extends StandAppBaseActionBarActivity implements GCMHelperListener, UserInfoListener, OnFragmentCreatedListener {

    // [START auth_variable_references]
    private static final int REQUEST_OAUTH = 1;

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    @InjectView(R.id.display)
    TextView mDisplay;

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    @Inject
    GooglePlayServicesHelper googlePlayServicesHelper;

    @Inject
    GoogleFitAPIHelper googleFitAPIHelper;

    @Inject
    UserHelper userHelper;

    @Inject
    GCMHelper gcmHelper;

    @Inject
    UserInfo userInfo;

    @Inject
    UserInfoMediator userInfoMediator;

    @Inject
    PreferenceAccess preferenceAccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        userInfoMediator.registerUserInfoListener(this);

        pager.setCurrentItem(1);
        pager.setOffscreenPageLimit(2); //never unload fragments, there's only 3 anyways

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_unregister_listener) {
//            unregisterFitnessDataListener();
            return true;
        }

        if (id == R.id.action_unregister_googlefitapi) {
            PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(googleFitAPIHelper.getClient());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");
            mDisplay.append("Google fit api connected!");

            if (!preferenceAccess.getUserAccount().isEmpty()){
                // Initialize the ViewPager and set an adapter
                pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

                // Bind the tabs to the ViewPager
                tabs.setViewPager(pager);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            // If your connection to the sensor gets lost at some point,
            // you'll be able to determine the reason and react to it here.
            if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                Log.i(LogConstants.LOG_ID, "Connection lost.  Cause: Network Lost.");
            } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                Log.i(LogConstants.LOG_ID, "Connection lost.  Reason: Service Disconnected");
            }
        }
    };

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.i(LogConstants.LOG_ID, "Connection failed. Cause: " + result.toString());
            if (!result.hasResolution()) {
                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), MainActivity.this, 0).show();
                mDisplay.append("Google fit api failed, no resoltion!");
                return;
            }
            if (!authInProgress) {
                try {
                    Log.i(LogConstants.LOG_ID, "Attempting to resolve failed connection");
                    authInProgress = true;
                    result.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(LogConstants.LOG_ID, "Exception while starting resolution activity", e);
                }
            }
        }
    };

    private void logMsg(String msg) {
        Log.d(LogConstants.LOG_ID, msg);
        mDisplay.append(msg + "\n");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        googlePlayServicesHelper.checkPlayServices(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(LogConstants.LOG_ID, "MainActivity:onStart Connecting fitness api...");
        googleFitAPIHelper.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleFitAPIHelper.isConnected()) {
            googleFitAPIHelper.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Triggered from onConnectionFailedListener resolution
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                preferenceAccess.updateUserAccount(accountName);

                if (!googleFitAPIHelper.isConnecting() && !googleFitAPIHelper.isConnected()) {
                    googleFitAPIHelper.connect();
                }
            } else {
                // TODO WTF
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }


    // Send an upstream message.
    public void onClick(final View view) {
        if (view == findViewById(R.id.send)) {
            gcmHelper.getAsyncTaskSendGCMMessage(mDisplay).execute(null, null, null);
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userInfoMediator.unregisterUserInfoListener(this);
    }


    @Override
    public void onRegisterSuccess(String regId) {
        logMsg("Device registered (persisted), registration ID=" + regId);
    }

    @Override
    public void onRegisterFailure(String regId) {
        logMsg("Unable to persist regid to local storage. unable to register");
        startGenericErrorActivity();
    }

    @Override
    public void onRequestSent(String regId) {
        logMsg("Request sent " + regId);
    }

    @Override
    public void onRequestNotSent(String regId) {
        logMsg("Failed registered " + regId + ". Request not sent");
        startGenericErrorActivity();
    }

    private void startGenericErrorActivity() {
        replaceThisActivity(GenericErrorActivity.class);
    }

    @Override
    public void onAlreadyRegistered(String regId) {
        logMsg("onAlreadyRegistered " + regId);
    }

    @Override
    public void onUserRefreshed(User user) {
        logMsg("user exists " + user.toString());
        preferenceAccess.updateUserId(user.get_id());

        if (googlePlayServicesHelper.checkPlayServices(this)) {
            gcmHelper.init(this, user.get_id());
        } else {
            Log.i(LogConstants.LOG_ID, "No valid Google Play Services APK found.");
            // TODO Throw exception
        }

    }

    @Override
    public void onEmailMissing(String userEmail) {
        logMsg("user missing " + userEmail);
    }

    @Override
    public void onUserNotFound(String userEmail) {
        logMsg("user not found " + userEmail);
        gcmHelper.clearRegId();
        startChromeExtensionErrorActivity();
    }

    private void startChromeExtensionErrorActivity() {
        replaceThisActivity(ChromeExtErrorActivity.class);
    }

    private void replaceThisActivity(Class classActivity) {
        Intent intent = new Intent(this, classActivity);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    public void onFragmentCreated() {
        // We don't want every child fragment to fetch user data all the time, just do it once and
        String userAccount = preferenceAccess.getUserAccount();
        if (!userAccount.isEmpty() && userInfo.getUser() == null){
            userHelper.refreshUser(userAccount);
        }
    }

}
