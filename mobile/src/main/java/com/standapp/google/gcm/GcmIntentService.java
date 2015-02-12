package com.standapp.google.gcm;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.standapp.R;
import com.standapp.activity.MainActivity;
import com.standapp.backend.BackendServer;
import com.standapp.backend.StandAppMessages;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.logger.LogConstants;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public static final int NOTIFICATION_SESSION_ID = 187;
    public static final String SESSION_WALKING_ID = "sa_walking_id";

    private static final String DATE_FORMAT = "yyyy.MM.dd G 'at' HH:mm:ss z";
//    private static final DataType TYPE_STEP_COUNT_CUMULATIVE = DataType.TYPE_STEP_COUNT_CUMULATIVE;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private GoogleFitAPIHelper googleFitAPIHelper;
    private PendingIntent mainActivityContentIntent;
    private StandAppMessages typeOfWork = null;
    private Intent receivedMsgIntent; // key for wakeelock


    public GcmIntentService() {
        super("GcmIntentService");
        googleFitAPIHelper = new GoogleFitAPIHelper(this);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        this.receivedMsgIntent = intent;

        mainActivityContentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i(LogConstants.LOG_ID, "Received: " + extras.toString());
                sendNotification("Received: " + extras.toString());

                if (extras.getString(BackendServer.GCM_FIELD_MESSAGE_KEY).equalsIgnoreCase(StandAppMessages.BREAK_START.toString())){
                    setTypeOfWork(StandAppMessages.BREAK_START);
                    initFitnessClientAndConnect();
                } else if (extras.getString(BackendServer.GCM_FIELD_MESSAGE_KEY).equalsIgnoreCase(StandAppMessages.BREAK_END.toString())){
                    setTypeOfWork(StandAppMessages.BREAK_END);
                    initFitnessClientAndConnect();
                } else {
                    releaseWakeLock();
                }
            }
        }

    }

    private void releaseWakeLock() {
        GcmBroadcastReceiver.completeWakefulIntent(this.receivedMsgIntent);
    }

    private void startWorkout() {
        subscribeToSteps();
        createSession();
        createRecordingNotification();
    }

    private void endWorkout() {
        endSession();
        unsubscribeFromSteps();
        clearRecordingNotification();
        disconnectFitnessClient();
    }

    private void unsubscribeFromSteps() {
        unsubscribe(DataType.TYPE_STEP_COUNT_DELTA);
        unsubscribe(DataType.TYPE_STEP_COUNT_CADENCE);
        unsubscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE);
    }

    private void disconnectFitnessClient() {
        if (googleFitAPIHelper.getClient().isConnected()) {
            googleFitAPIHelper.getClient().disconnect();
        }
    }

    private void endSession() {
        PendingResult<SessionStopResult> pendingResult = Fitness.SessionsApi.stopSession(googleFitAPIHelper.getClient(), SESSION_WALKING_ID);
        pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
            @Override
            public void onResult(SessionStopResult sessionStopResult) {
                Log.i(LogConstants.LOG_ID, sessionStopResult.toString());
            }});
    }

    private void clearRecordingNotification() {
        mNotificationManager.cancel(NOTIFICATION_SESSION_ID);
    }

    private void initFitnessClientAndConnect() {
        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
        if (!googleFitAPIHelper.isConnected() && !googleFitAPIHelper.isConnecting() ){
            googleFitAPIHelper.connect();
        }
    }

    private void createSession() {
        Session s = getBeginSession();
        PendingResult<Status> pendingResult = Fitness.SessionsApi.startSession(googleFitAPIHelper.getClient(), s);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.i(LogConstants.LOG_ID, "Successfully subscribed!");
                } else {
                    Log.i(LogConstants.LOG_ID, "There was a problem subscribing.");
                }
            }});
    }

    private Session getBeginSession() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        // TODO refactor these into constants
        return new Session.Builder()
                    .setName("sa_walking")
                    .setIdentifier(SESSION_WALKING_ID)
                    .setDescription("StandApp walking")
                    .setStartTime(cal.getTimeInMillis(), TimeUnit.MILLISECONDS)
                    // optional - if your app knows what activity:
                    .setActivity(FitnessActivities.WALKING)
                    .build();
    }


    private void createRecordingNotification() {
        // inform user with ongoing notification that session is being recorded.
        String msg = getResources().getString(R.string.notif_in_session_msg);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.sa_ic_applauncher)
                        .setContentTitle(getResources().getString(R.string.notif_in_session_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setOngoing(true)
                        .setContentText(msg);

        mBuilder.setContentIntent(mainActivityContentIntent); // FIXME JS Send msg to end request? @JB
        mNotificationManager.notify(NOTIFICATION_SESSION_ID, mBuilder.build());
    }


    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");

            if (typeOfWork == StandAppMessages.BREAK_START) {
                startWorkout();
                // TODO js sleep this thread for and end workout if it goes to long or
                // TODO js let user tap to kill workout session
            } else if (typeOfWork == StandAppMessages.BREAK_END) {
                endWorkout();
            }

            releaseWakeLock();
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

    GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        // Called whenever the API client fails to connect.
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.i(LogConstants.LOG_ID, "Connection failed. Cause: " + result.toString());
            if (!result.hasResolution()) {
                sendNotificationOAuthResoluton();
                return;
            }
            sendNotificationOAuthResoluton();
        }
    };

    private void sendNotification(String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_ic_googleplayservices)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(mainActivityContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void sendNotificationOAuthResoluton() {
        String msg = getResources().getString(R.string.notif_oauth_msg);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_signin_btn_icon_disabled_dark)
                        .setContentTitle(getResources().getString(R.string.notif_oauth_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText( msg);

        mBuilder.setContentIntent(mainActivityContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    private void subscribeToSteps() {
        subscribe(DataType.TYPE_STEP_COUNT_DELTA);
        subscribe(DataType.TYPE_STEP_COUNT_CADENCE);
        subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE);
    }

    private void subscribe(DataType dataType) {
        PendingResult<Status> statusPendingResult = Fitness.RecordingApi.subscribe(googleFitAPIHelper.getClient(), dataType);
        statusPendingResult
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(LogConstants.LOG_ID, "Existing subscription for activity detected.");
                            } else {
                                Log.i(LogConstants.LOG_ID, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(LogConstants.LOG_ID, "There was a problem subscribing.");
                        }
                    }
                });
    }

    private void unsubscribe(final DataType dataType) {
        PendingResult<Status> unsubscribe = Fitness.RecordingApi.unsubscribe(googleFitAPIHelper.getClient(), dataType);
        unsubscribe.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(LogConstants.LOG_ID, "Successfully unsubscribed for data type: " + dataType.getName());
                        } else {
                            // Subscription not removed
                            Log.i(LogConstants.LOG_ID, "Failed to unsubscribe for data type: " + dataType.getName());
                        }
                    }
                });
    }

    public void setTypeOfWork(StandAppMessages typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

}