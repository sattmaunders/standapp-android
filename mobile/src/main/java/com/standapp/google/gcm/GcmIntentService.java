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
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.standapp.R;
import com.standapp.activity.MainActivity;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.logger.LogConstants;

import java.text.SimpleDateFormat;
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
    private static final String DATE_FORMAT = "yyyy.MM.dd G 'at' HH:mm:ss z";
    private static final DataType TYPE_STEP_COUNT_CUMULATIVE = DataType.TYPE_STEP_COUNT_CUMULATIVE;
    public static final String SA_WALKING_ID = "sa_walking_id";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private GoogleFitAPIHelper googleFitAPIHelper;
    private PendingIntent mainActivityContentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
    private String typeOfWork = null; // TODO js refactor to enum
    private Intent receivedMsgIntent; // key for wakeelock


    public GcmIntentService() {
        super("GcmIntentService");
        googleFitAPIHelper = new GoogleFitAPIHelper(this);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        this.receivedMsgIntent = intent;
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
                // FIXME JS make enums and confirm API with @JB
                Log.i(LogConstants.LOG_ID, "Received: " + extras.toString());
                sendNotification("Received: " + extras.toString());

                if (extras.getString("message").equalsIgnoreCase("startworkout")){
                    setTypeOfWork("startworkout");
                    initFitnessClientAndConnect();
                } else if (extras.getString("message").equalsIgnoreCase("endworkout")){
                    setTypeOfWork("startworkout");
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

    private void disconnectFitnessClient() {
        if (googleFitAPIHelper.getClient().isConnected()) {
            googleFitAPIHelper.getClient().disconnect();
        }
    }

    private void endSession() {
        PendingResult<SessionStopResult> pendingResult = Fitness.SessionsApi.stopSession(googleFitAPIHelper.getClient(), SA_WALKING_ID);
        pendingResult.await();
        // TODO check the result;
    }

    private void clearRecordingNotification() {
        // TODO removed ungoing notification
    }

    private void initFitnessClientAndConnect() {
        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
        googleFitAPIHelper.connect();
    }

    private void createSession() {
        Session s = getBeginSession();
        PendingResult<Status> pendingResult = Fitness.SessionsApi.startSession(googleFitAPIHelper.getClient(), s);
        pendingResult.await();
        // TODO check the result;
    }

    private Session getBeginSession() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        // TODO refactor these into constants
        return new Session.Builder()
                    .setName("sa_walking")
                    .setIdentifier(SA_WALKING_ID)
                    .setDescription("StandApp walking")
                    .setStartTime(cal.getTimeInMillis(), TimeUnit.MILLISECONDS)
                    // optional - if your app knows what activity:
                    .setActivity(FitnessActivities.WALKING)
                    .build();
    }


    private void createRecordingNotification() {
        // inform user with ongoing notification that session is being recorded.
    }


    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");

            if (typeOfWork == "startworkout") {
                startWorkout();
                // TODO js sleep this thread for and end workout if it goes to long or
                // TODO js let user tap to kill workout session
            } else if (typeOfWork == "endworkout") {
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
                        .setSmallIcon(R.drawable.sa_ic_applauncher)
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
                        .setSmallIcon(R.drawable.sa_ic_applauncher)
                        .setContentTitle(getResources().getString(R.string.notif_oauth_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText( msg);

        mBuilder.setContentIntent(mainActivityContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void readSteps() {
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -1);
        long startTime = cal.getTimeInMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Log.i(LogConstants.LOG_ID, "Range Start: " + dateFormat.format(startTime));
        Log.i(LogConstants.LOG_ID, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .read(TYPE_STEP_COUNT_CUMULATIVE)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(googleFitAPIHelper.getClient(), readRequest).await(1, TimeUnit.MINUTES);
        dumpDataSet(dataReadResult.getDataSet(TYPE_STEP_COUNT_CUMULATIVE));
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(LogConstants.LOG_ID, "Data returned for Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(LogConstants.LOG_ID, "Data point:");
            Log.i(LogConstants.LOG_ID, "\tType: " + dp.getDataType().getName());
            Log.i(LogConstants.LOG_ID, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(LogConstants.LOG_ID, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(LogConstants.LOG_ID, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private void subscribeToSteps() {
        PendingResult<Status> statusPendingResult = Fitness.RecordingApi.subscribe(googleFitAPIHelper.getClient(), TYPE_STEP_COUNT_CUMULATIVE);
        statusPendingResult.await();
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

    private void unsubscribeFromSteps() {
        PendingResult<Status> unsubscribe = Fitness.RecordingApi.unsubscribe(googleFitAPIHelper.getClient(), TYPE_STEP_COUNT_CUMULATIVE);
        unsubscribe.await();
        unsubscribe.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(LogConstants.LOG_ID, "Successfully unsubscribed for data type: " + TYPE_STEP_COUNT_CUMULATIVE.getName());
                        } else {
                            // Subscription not removed
                            Log.i(LogConstants.LOG_ID, "Failed to unsubscribe for data type: " + TYPE_STEP_COUNT_CUMULATIVE.getName());
                        }
                    }
                });
    }

    public void setTypeOfWork(String typeOfWork) {
        // tODO should be enum
        this.typeOfWork = typeOfWork;
    }

}