package com.standapp.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.standapp.LockScreenActivity;
import com.standapp.R;
import com.standapp.activity.common.StandAppBaseActionBarActivity;
import com.standapp.activity.error.ChromeExtErrorActivity;
import com.standapp.activity.error.GenericErrorActivity;
import com.standapp.backend.UserHelper;
import com.standapp.backend.UserHelperListener;
import com.standapp.google.GooglePlayServicesHelper;
import com.standapp.google.gcm.GCMHelper;
import com.standapp.google.gcm.GCMHelperListener;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.logger.Log;
import com.standapp.logger.LogConstants;
import com.standapp.logger.LogWrapper;
import com.standapp.logger.MessageOnlyLogFilter;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends StandAppBaseActionBarActivity implements GCMHelperListener, UserHelperListener {

    // [START auth_variable_references]
    private static final int REQUEST_OAUTH = 1;
    public static final String SERVER_BASE_URL = "http://standapp-2015.herokuapp.com";

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private boolean isLocked = false;
    private boolean isReady = false;
    private boolean isAway = false;


    /**
     * Tag used on log messages.
     */
    public static final String TAG = "StandApp";

    @InjectView(R.id.display)
    TextView mDisplay;

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    Context context;

    @Inject
    GooglePlayServicesHelper googlePlayServicesHelper;

    @Inject
    GoogleFitAPIHelper googleFitAPIHelper;


//    private GoogleApiClient mClient = null;
    // [END auth_variable_references]

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    private OnDataPointListener mListener;
    private boolean connectedToFitAPI = false;
    private JSONObject user;

//    private boolean stepCounterListenerRegistered;
    // [END mListener_variable_reference]


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

        return super.onOptionsItemSelected(item);
    }


    @Inject
    UserHelper userHelper;

    @Inject
    GCMHelper gcmHelper;

    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");
            mDisplay.append("Google fit api connected!");
        }

        @Override
        public void onConnectionSuspended(int i) {
            // If your connection to the sensor gets lost at some point,
            // you'll be able to determine the reason and react to it here.
            if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                com.standapp.logger.Log.i(LogConstants.LOG_ID, "Connection lost.  Cause: Network Lost.");
            } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                com.standapp.logger.Log.i(LogConstants.LOG_ID, "Connection lost.  Reason: Service Disconnected");
            }
        }
    };

    GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            com.standapp.logger.Log.i(LogConstants.LOG_ID, "Connection failed. Cause: " + result.toString());
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the ViewPager and set an adapter
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        // Bind the tabs to the ViewPager
        tabs.setViewPager(pager);
        userHelper.checkIfUserIsCreated(this);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);

    }


    private void logMsg(String msg) {
        Log.d(LogConstants.LOG_ID, msg);
        mDisplay.append(msg + "\n");
    }


    private void inquireStatusServerLoop() {
        final Handler h = new Handler();
        final int delay = 5000; //milliseconds

        h.postDelayed(new Runnable() {
            public void run() {
                //do something
                inquireStatusServer();
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    private void inquireStatusServer() {
        // Every 5 sec, we ask server for status
        // If True, then start workout (step counter)
        // If False, stop the work out
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = SERVER_BASE_URL + "/status";
        Log.i(TAG, "Inquiring status of lock screen");

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Received response for status");
                        boolean isServerLocked = false;
                        boolean isServerAway = false;
                        boolean isServerReady = false;

                        try {
                            isServerLocked = !response.getBoolean("unlocked");
                            isServerAway = response.getBoolean("away");
                            isServerReady = response.getBoolean("readyToUnlock");

                            if (isAway != isServerAway) {
                                Log.i(TAG, "Server away status has been changed to:" + isServerAway);
                                isAway = isServerAway;

                                if (isAway) {
                                    // Sending notification to phone with options
                                    askUserToLockScreen();
                                }
                            }

                            if (isReady != isServerReady && isLocked) {
                                isReady = isServerReady;

                                if (isReady) {
                                    Log.i(TAG, "User is ready to unlock screen");
                                    tellUserHeCanGoBackToDesk();
                                }
                            }

                            if (isLocked != isServerLocked && !isAway) {
                                Log.i(TAG, "Server status has changed to:" + isServerLocked);
                                isLocked = isServerLocked;

                                if (isLocked) {
                                    if (connectedToFitAPI) {
                                        Log.i(TAG, "Queue notifications to watch.");
                                        lockScreen();
                                    } else if (!connectedToFitAPI) {
                                        Log.i(TAG, "Not yet connected to FIT API... Unable to find fitness data sources");
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Failed response");
            }
        });
        queue.add(stringRequest);
    }

    private void tellUserHeCanGoBackToDesk() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification noti = new Notification.Builder(this)
                .setContentTitle("Mission Accomplished")
                .setContentText("You can go back to your desktop").setSmallIcon(R.drawable.ic_mission_accomplished)
                .setContentIntent(pIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        noti.defaults |= Notification.DEFAULT_SOUND;
        noti.defaults |= Notification.DEFAULT_VIBRATE;

        Log.i(TAG, "Sent notification to indicate the user can go back to desktop");
        notificationManager.notify(0, noti);
    }

    private void lockScreen() {
        sendNotificationOfLockedScreen();
    }

    private void sendNotificationOfLockedScreen() {
        // Prepare intent which is triggered if the
        // notification is selected

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("doLock", true);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Screen locked!")
                .setContentText("Start your workout now to unlock screen").setSmallIcon(R.drawable.ic_exercise)
                .setContentIntent(pIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.ic_no, "Ignore", pIntent).build();
//        noti.contentView.setImageViewResource(android.R.id.icon, R.drawable.ic_exercise);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        noti.defaults |= Notification.DEFAULT_SOUND;
        noti.defaults |= Notification.DEFAULT_VIBRATE;

        Log.i(TAG, "Sent notification that screen is locked");
        notificationManager.notify(0, noti);
    }

    private void askUserToLockScreen() {
        // Prepare intent which is triggered if the
        // notification is selected

        Intent intentMainActivity = new Intent(this, MainActivity.class);
        PendingIntent pIntentMainActivity = PendingIntent.getActivity(this, 0, intentMainActivity, 0);

        Intent intent = new Intent(this, LockScreenActivity.class);
        PendingIntent lockScreenActivity = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Leaving your computer?")
                .setContentText("Would you like to lock your computer.").setSmallIcon(R.drawable.ic_exercise)
                .setContentIntent(lockScreenActivity)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.ic_yes, "Yes", lockScreenActivity)
                .addAction(R.drawable.ic_no, "No", pIntentMainActivity).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        noti.defaults |= Notification.DEFAULT_SOUND;
        noti.defaults |= Notification.DEFAULT_VIBRATE;

        Log.i(TAG, "Asked user to lock screen.");
        notificationManager.notify(1, noti);
    }

    // [END auth_oncreate_setup_ending]

    // [START auth_build_googleapiclient_beginning]


    // [END auth_build_googleapiclient_ending]

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        googlePlayServicesHelper.checkPlayServices(this);
    }

    // [START auth_connection_flow_in_activity_lifecycle_methods]
    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting fitness api...");
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
                if (!googleFitAPIHelper.isConnecting() && !googleFitAPIHelper.isConnected()) {
                    googleFitAPIHelper.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }


    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     * If the application cares about a data type but doesn't care about the source of the data,
     * this can be skipped entirely, instead calling
     * {@link com.google.android.gms.fitness.SensorsApi
     * #register(GoogleApiClient, SensorRequest, DataSourceListener)},
     * where the {@link SensorRequest} contains the desired data type.
     */
//    private void findFitnessDataSources() {
//        // [START find_data_sources]
//        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
//                // At least one datatype must be specified.
//                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
//                        // Can specify whether data type is raw or derived.
//                .setDataSourceTypes(DataSource.TYPE_RAW)
//                .build())
//                .setResultCallback(new ResultCallback<DataSourcesResult>() {
//                    @Override
//                    public void onResult(DataSourcesResult dataSourcesResult) {
//                        Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString());
//                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
//                            Log.i(TAG, "Data source found: " + dataSource.toString());
//                            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());
//
//                            //Let's register a listener to receive Activity data!
//                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)
//                                    && mListener == null) {
//                                Log.i(TAG, "Data source for TYPE_STEP_COUNT_CUMULATIVE found!  Registering.");
//                                registerFitnessDataListener(dataSource,
//                                        DataType.TYPE_STEP_COUNT_CUMULATIVE);
//                            }
//                        }
//                    }
//                });
//        // [END find_data_sources]
//    }

    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
//    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
//        // [START register_data_listener]
//        mListener = new OnDataPointListener() {
//            @Override
//            public void onDataPoint(DataPoint dataPoint) {
//                for (Field field : dataPoint.getDataType().getFields()) {
//                    Value val = dataPoint.getValue(field);
//                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
//                    Log.i(TAG, "Detected DataPoint value: " + val);
//
//                    sendStepsToServer(val);
//                }
//            }
//        };
//
//        Fitness.SensorsApi.add(
//                mClient,
//                new SensorRequest.Builder()
//                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
//                        .setDataType(dataType) // Can't be omitted.
//                        .setSamplingRate(3, TimeUnit.SECONDS)
//                        .build(),
//                mListener)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        if (status.isSuccess()) {
//                            Log.i(TAG, "Listener registered!");
//                        } else {
//                            Log.i(TAG, "Listener not registered.");
//                        }
//                    }
//                });
//        // [END register_data_listener]
//    }
    private void sendStepsToServer(final Value val) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = SERVER_BASE_URL + "/steps/" + val.toString();
        Log.i(TAG, "Sending steps to server");

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Send steps to server " + val.toString());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Failed response");
            }
        });
        queue.add(stringRequest);
    }

    /**
     * Unregister the listener with the Sensors API.
     */
//    private void unregisterFitnessDataListener() {
//        if (mListener == null) {
//            // This code only activates one listener at a time.  If there's no listener, there's
//            // nothing to unregister.
//            return;
//        }
//
//        // [START unregister_data_listener]
//        // Waiting isn't actually necessary as the unregister call will complete regardless,
//        // even if called from within onStop, but a callback can still be added in order to
//        // inspect the results.
//        Fitness.SensorsApi.remove(
//                mClient,
//                mListener)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        if (status.isSuccess()) {
//                            Log.i(TAG, "Listener was removed!");
//                        } else {
//                            Log.i(TAG, "Listener was not removed.");
//                        }
//                    }
//                });
//        // [END unregister_data_listener]
//    }


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
    }


    /**
     * Initialize a custom log class that outputs both to in-app targets and logcat.
     */
    private void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        // On screen logging via a customized TextView.
//        LogView logView = (LogView) findViewById(R.id.sample_logview);
//        logView.setTextAppearance(this, R.style.Log);
//        logView.setBackgroundColor(Color.WHITE);
//        msgFilter.setNext(logView);
        Log.i(TAG, "Ready");
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
    // TODO pass in User POJO instead of JSONObject
    public void onUserExists(JSONObject user) {
        logMsg("user exists " + user.toString());

        this.user = user; //Store the user in MainActivity for later usage.

        if (googlePlayServicesHelper.checkPlayServices(this)) {
            try {
                gcmHelper.init(this, user.getString("_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
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
}
