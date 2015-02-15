package com.standapp.google.gcm;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.standapp.R;
import com.standapp.activity.MainActivity;
import com.standapp.backend.BackendServer;
import com.standapp.backend.SenderId;
import com.standapp.backend.StandAppMessages;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.logger.LogConstants;
import com.standapp.preferences.PreferenceAccess;

import org.json.JSONObject;

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
    public static final int NOTIFICATION_OAUTH_RES_ID = 1;
    public static final int NOTIFICATION_SESSION_ID = 1872;

    // Dependencies not injected :(  // TODO should use DI dagger injection.
    private final PreferenceAccess preferenceAccess;
    private BackendServer backendServer;
    private GoogleFitAPIHelper googleFitAPIHelper;


    private NotificationManager mNotificationManager;
    private PendingIntent mainActivityContentIntent;
    private StandAppMessages typeOfWork = null;

    private static final boolean SUBSCRIBE_TO_STEPS = false;
    private static final boolean UNSUBSCRIBE_TO_STEPS = false;
    private Intent intent;


    public GcmIntentService() {
        super("GcmIntentService");
        preferenceAccess = new PreferenceAccess(this);
        googleFitAPIHelper = new GoogleFitAPIHelper(this, preferenceAccess);
    }


    /**
     * FIXME JS when is a better time to release the wakelock? maybe when the session has started for when
     * it is a startEvent, and when session has ended when.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        this.intent = intent;
        mainActivityContentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        // TODO js dont use null check to determine if this service was invoked by user tap on prior notifaction
        if (extras == null) {
            backendServer = new BackendServer(Volley.newRequestQueue(this));
            sendMessageToChromeToEndBreak();
            initFitnessClientAndConnect(StandAppMessages.BREAK_END);
        } else {
            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType) && !messageOriginatedFromPhone(extras)) {
                    Log.i(LogConstants.LOG_ID, "Received: " + extras.toString());
                    if (extras.getString(BackendServer.GCM_FIELD_MESSAGE_KEY).equalsIgnoreCase(StandAppMessages.BREAK_START.toString())) {
                        initFitnessClientAndConnect(StandAppMessages.BREAK_START);
                    } else if (extras.getString(BackendServer.GCM_FIELD_MESSAGE_KEY).equalsIgnoreCase(StandAppMessages.BREAK_END.toString())) {
                        initFitnessClientAndConnect(StandAppMessages.BREAK_END);
                    }
                } else if (messageOriginatedFromPhone(extras)) {
                    Log.i(LogConstants.LOG_ID, "Ignored a gcm message from own phone device");
                }
            }
        }

        // TODO confirm release lock doesn't mess up async calls
        if (typeOfWork == null){
            Log.i(LogConstants.LOG_ID, "Never set a type of work, so just releasing the lock");
            releaseWakeLock();
        }
    }

    private void disconnectGoogleFitAndReleaseWakeLock() {
        if (googleFitAPIHelper != null && googleFitAPIHelper.isConnected()) {
            googleFitAPIHelper.disconnect();
            Log.i(LogConstants.LOG_ID, "GcmService disconnected Google Fit");
        } else {
            Log.w(LogConstants.LOG_ID, "GcmService unable to disconnect from Google Fit becasue its not connected");
        }
        releaseWakeLock();
    }

    private boolean messageOriginatedFromPhone(Bundle extras) {
        return extras.getString(BackendServer.GCM_FIELD_SENDER_ID) != null && extras.getString(BackendServer.GCM_FIELD_SENDER_ID).equalsIgnoreCase(SenderId.PHONE.toString());
    }

    private void sendMessageToChromeToEndBreak() {
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                Log.i(LogConstants.LOG_ID, "Successfully sent message to end break to server for chrome ext");
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(LogConstants.LOG_ID, "Unable to send message to end break to server for chrome ext");
            }
        };

        String userId = preferenceAccess.getUserId();
        if (!userId.isEmpty()) {
            backendServer.endBreak(userId, successListener, errorListener);
        }
    }

    private void releaseWakeLock() {
        Log.i(LogConstants.LOG_ID, "Releasing wakelock");
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void startWorkout() {
        subscribeToSteps();
        createSession();
        createRecordingNotification();
    }

    private void endWorkout() {
        endSession();
        unsubscribeFromSteps(); // TODO confirm unsubscribing destroys data
        clearRecordingNotification();
    }

    private void unsubscribeFromSteps() {
        if (UNSUBSCRIBE_TO_STEPS) {
            unsubscribe(DataType.TYPE_STEP_COUNT_DELTA);
            unsubscribe(DataType.TYPE_STEP_COUNT_CADENCE);
            unsubscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE);
        }
    }

    private void endSessionAndCreateNewOne(String oldSessionId) {
        Log.i(LogConstants.LOG_ID, "Ending session (" + oldSessionId + ") and creating a new one");

        if (googleFitAPIHelper.isConnected()) {
            PendingResult<SessionStopResult> pendingResult = googleFitAPIHelper.stopSession(oldSessionId);
            pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
                @Override
                public void onResult(SessionStopResult sessionStopResult) {
                    Log.i(LogConstants.LOG_ID, "endSession toString: " + sessionStopResult.toString() + " endSession code " + sessionStopResult.getStatus().getStatusCode());
                    if (sessionStopResult.getSessions() != null && sessionStopResult.getSessions().size() > 0) {
                        Log.i(LogConstants.LOG_ID, "Ended some session properly, # of sessions ended " + sessionStopResult.getSessions().size());
                    }
                    if (preferenceAccess.updateLastFitSessionId("")) {
                        Log.i(LogConstants.LOG_ID, "Updated session id preferences by clearing it");
                        createSession();
                    }
                }
            });
        } else {
            Log.w(LogConstants.LOG_ID, "Unable to endSessionAndCreateNewOne, Google Fit was disconnected");
        }
    }


    private void endSession() {
        String lastFitSessionId = preferenceAccess.getLastFitSessionId();
        Log.i(LogConstants.LOG_ID, "Trying to end session for " + lastFitSessionId);

        if (!lastFitSessionId.isEmpty()) {
            PendingResult<SessionStopResult> pendingResult = googleFitAPIHelper.stopSession(lastFitSessionId);
            pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
                @Override
                public void onResult(SessionStopResult sessionStopResult) {
                    Log.i(LogConstants.LOG_ID, "endSession toString: " + sessionStopResult.toString() + " endSession code " + sessionStopResult.getStatus().getStatusCode());
                    preferenceAccess.updateLastFitSessionId("");
                    disconnectGoogleFitAndReleaseWakeLock();
                }
            });
        } else {
            if (lastFitSessionId.isEmpty()) {
                Log.i(LogConstants.LOG_ID, "Unable to end empty session id");
            } else if (!googleFitAPIHelper.isConnected()) {
                Log.w(LogConstants.LOG_ID, "Unable to end session because app not connected");
            }
            disconnectGoogleFitAndReleaseWakeLock();
        }

    }

    private void clearRecordingNotification() {
        mNotificationManager.cancel(NOTIFICATION_SESSION_ID);
    }

    private void initFitnessClientAndConnect(StandAppMessages typeOfWork) {
        setTypeOfWork(typeOfWork);
        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
        if (!googleFitAPIHelper.isConnected() && !googleFitAPIHelper.isConnecting()) {
            googleFitAPIHelper.blockConnect();
        }
    }

    private void createSession() {
        final Session beginSession = getBeginSession();

        // Make sure there's no active session
        String lastFitSessionId = preferenceAccess.getLastFitSessionId();
        Log.i(LogConstants.LOG_ID, "When creating session, the last session id: " + lastFitSessionId);


        // FIXME make sure client is connected
        if (lastFitSessionId.isEmpty() && googleFitAPIHelper.isConnected()) {
            PendingResult<Status> pendingResult = googleFitAPIHelper.startSession(beginSession);
            pendingResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(LogConstants.LOG_ID, "Successfully started session!");
                        preferenceAccess.updateLastFitSessionId(beginSession.getIdentifier());
                        Log.i(LogConstants.LOG_ID, "Updated preferences with " + beginSession.getIdentifier());

                    } else {
                        Log.i(LogConstants.LOG_ID, "There was a problem subscribing. Code" + status.getStatusCode());
                    }
                    disconnectGoogleFitAndReleaseWakeLock();
                }
            });
        } else {
            if (!lastFitSessionId.isEmpty()) {
                Log.i(LogConstants.LOG_ID, "A session ( " + lastFitSessionId + ")already existed, end it, and create a new one");
                endSessionAndCreateNewOne(lastFitSessionId); // Watchtout, can go in inifinite recursion
            } else if (!googleFitAPIHelper.isConnected()) {
                Log.w(LogConstants.LOG_ID, "Unable to start session b/c client is not connected!");
                disconnectGoogleFitAndReleaseWakeLock();
            }

        }


    }

    private Session getBeginSession() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        // TODO refactor these into constants
        Session session = new Session.Builder()
                .setName("StandApp Walking")
                .setDescription("StandApp monitors your walking to meet your office health goals") // TODO string resource?
                .setStartTime(cal.getTimeInMillis(), TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.WALKING) // optional - if your app knows what activity:
                .build();
        return session;
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

        PendingIntent endWorkoutPendingIntent = PendingIntent.getService(this, 0, new Intent(this, GcmIntentService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(endWorkoutPendingIntent);
        mNotificationManager.notify(NOTIFICATION_SESSION_ID, mBuilder.build());
    }


    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");

            if (typeOfWork == StandAppMessages.BREAK_START) {
                startWorkout();
                // TODO js sleep this thread for and end workout if it goes to long or
            } else if (typeOfWork == StandAppMessages.BREAK_END) {
                endWorkout();
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
            releaseWakeLock();
        }
    };

    GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        // Called whenever the API client fails to connect.
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.i(LogConstants.LOG_ID, "Connection failed. Cause: " + result.toString());
            sendNotificationOAuthResolution();
            releaseWakeLock();
            return;
        }
    };

    private void sendNotificationOAuthResolution() {
        String msg = getResources().getString(R.string.notif_oauth_msg);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.sa_ic_fit)
                        .setContentTitle(getResources().getString(R.string.notif_oauth_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(mainActivityContentIntent);
        mNotificationManager.notify(NOTIFICATION_OAUTH_RES_ID, mBuilder.build());
    }


    private void subscribeToSteps() {
        // TODO JS Confirm that this is messing up the counting of steps for the original app.
        // This is disabled b/c it seems to intefere with the step counter in the original.
        if (SUBSCRIBE_TO_STEPS) {
            subscribe(DataType.TYPE_STEP_COUNT_DELTA);
            subscribe(DataType.TYPE_STEP_COUNT_CADENCE);
            subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE);
        }

    }

    private void subscribe(DataType dataType) {
        PendingResult<Status> statusPendingResult = googleFitAPIHelper.subscribe(dataType);
        statusPendingResult.setResultCallback(new ResultCallback<Status>() {
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
        if (googleFitAPIHelper.isConnected()) {
            PendingResult<Status> unsubscribe = googleFitAPIHelper.unsubscribe(dataType);
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
        } else {
            Log.i(LogConstants.LOG_ID, "Fit client not connected, unable to unsubscribe");
        }

    }

    public void setTypeOfWork(StandAppMessages typeOfWork) {
        this.typeOfWork = typeOfWork;
        Log.i(LogConstants.LOG_ID, "Type of work " + typeOfWork.toString());
    }

}