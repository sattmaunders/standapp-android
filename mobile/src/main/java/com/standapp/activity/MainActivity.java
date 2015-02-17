package com.standapp.activity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
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
public class MainActivity extends StandAppBaseActionBarActivity implements GCMHelperListener, UserInfoListener, OnFragmentCreatedListener, ViewPager.OnPageChangeListener {

    // [START auth_variable_references]
    private static final int REQUEST_OAUTH = 1;

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    public static final String INTENT_PARAM_USER_EMAIL = "USER_EMAIL";
    private static final String SELECTED_TAB_INDEX = "SELECTED_TAB_INDEX";
    private boolean authInProgress = false;

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    @InjectView(R.id.content)
    LinearLayout content;

    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

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
        initToolbar();

        userInfoMediator.registerUserInfoListener(this);

        tabs.setOnPageChangeListener(this);


        pager.setCurrentItem(1);
        pager.setOffscreenPageLimit(2); //never unload fragments, there's only 3 anyways


        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
            this.onPageSelected(savedInstanceState.getInt(SELECTED_TAB_INDEX));
        } else {
            this.onPageSelected(0);
        }
        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
        // Connect to the Fitness API
        Log.i(LogConstants.LOG_ID, "MainActivity:onStart Connecting fitness api...");
        googleFitAPIHelper.connect();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setLogo(R.drawable.sa_ic_applauncher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_go_to_site) {
            return goToWebsite();
        }

        if (id == R.id.action_settings) {
            return goToSettings();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean goToSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    private boolean goToWebsite() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.chrome_ext_error_link)));
        startActivity(i);
        return true;
    }


    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");

            if (!preferenceAccess.getUserAccount().isEmpty()){
                initFragments();
                // This will trigger onFragmentCreated
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

    private void initFragments() {
        // Initialize the ViewPager and set an adapter
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        // Customize tab appearance:
        tabs.setShouldExpand(true);
        tabs.setIndicatorColor(getResources().getColor(R.color.extAccent));
        tabs.setDividerColor(getResources().getColor(R.color.extHue1));
        // Bind the tabs to the ViewPager
        tabs.setViewPager(pager);
    }

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.i(LogConstants.LOG_ID, "Connection failed. Cause: " + result.toString());
            if (!result.hasResolution()) {
                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), MainActivity.this, 0).show();
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
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                startGenericErrorActivity();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
        outState.putInt(SELECTED_TAB_INDEX, pager.getCurrentItem());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userInfoMediator.unregisterUserInfoListener(this);

        googleFitAPIHelper.unregisterListeners(connectionCallbacks, onConnectionFailedListener);
        if (googleFitAPIHelper.isConnected()) {
            googleFitAPIHelper.disconnect();
        }

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
        replaceThisActivity(new Intent(this, GenericErrorActivity.class));
    }

    @Override
    public void onAlreadyRegistered(String regId) {
        logMsg("onAlreadyRegistered " + regId);
    }

    @Override
    public void onUserRefreshed(User user) {

        logMsg("user exists " + user.toString());
        preferenceAccess.updateUserId(user.get_id());
        progressBar.setVisibility(View.GONE);

        if (googlePlayServicesHelper.checkPlayServices(this)) {
            gcmHelper.init(this, user.get_id());
        } else {
            Log.i(LogConstants.LOG_ID, "No valid Google Play Services APK found.");
            Log.i(LogConstants.LOG_ID, "No valid Google Play Services APK found.");
            Toast.makeText(this, getString(R.string.no_google_play_services_apk), Toast.LENGTH_LONG);
        }

    }

    @Override
    public void onEmailMissing(String userEmail) {
        logMsg("user missing " + userEmail);
    }

    @Override
    public void onUserNotFound(String userEmail) {
        logMsg("user not found " + userEmail);
        progressBar.setVisibility(View.GONE);
        preferenceAccess.clearRegId(); // probably not needed here as the user will revoke permissions and cause a clear
        startChromeExtensionErrorActivity(userEmail);
    }

    @Override
    public void onNetworkError() {
        Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_LONG);
        startGenericErrorActivity();
    }

    private void startChromeExtensionErrorActivity(String userEmail) {
        Intent intent = new Intent(this, ChromeExtErrorActivity.class);
        intent.putExtra(INTENT_PARAM_USER_EMAIL, userEmail);
        replaceThisActivity(intent);
    }

    private void replaceThisActivity(Intent intent) {
        this.startActivity(intent);
        this.finish();
    }

    @Override
    public void onFragmentCreated() {
        // We don't want every child fragment to fetch user data all the time, just do it once and
        String userAccount = preferenceAccess.getUserAccount();
        if (!userAccount.isEmpty() && userInfo.getUser() == null){
            progressBar.setVisibility(View.VISIBLE);
            userHelper.refreshUser(userAccount);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.tabs_activity_graph)[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
