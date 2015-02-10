package com.standapp.google.gcm;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.standapp.backend.BackendServer;
import com.standapp.logger.LogConstants;
import com.standapp.preferences.PreferenceAccess;
import com.standapp.util.AppInfo;
import com.standapp.util.UserInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by John on 2/3/2015.
 */
public class GCMHelper {

    private String SENDER_ID = "665143645608";

    public static final String EXTRA_MESSAGE = "message";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GoogleCloudMessaging gcm;
    private String regId;
    private AtomicInteger msgId = new AtomicInteger();

    private PreferenceAccess preferenceAccess;
    private Activity activity; // TODO memory leak? unregister
    private BackendServer backendServer;

    private UserInfo userInfo;
    private GCMHelperListener gcmHelperListener; // TODO memory leak? unregister

    public GCMHelper(PreferenceAccess preferenceAccess, Activity activity, BackendServer backendServer, UserInfo userInfo) {
        this.preferenceAccess = preferenceAccess;
        this.activity = activity;
        this.backendServer = backendServer;
        this.userInfo = userInfo;
    }

    public void init(GCMHelperListener gcmHelperListener, String userId) {
        // TODO Fail if gcmHelperListener is null
        this.gcmHelperListener = gcmHelperListener;

        gcm = GoogleCloudMessaging.getInstance(activity);
        regId = getRegistrationId();

        if (regId.isEmpty()) {
            registerInBackground(userId);
        } else {
            gcmHelperListener.onAlreadyRegistered(regId);
            Log.i(LogConstants.LOG_ID, "Device already registered with " + regId);
            // TODO JS Show user is registered
        }
    }

    Response.Listener<JSONObject> gcmRegisterSuccessListener = new Response.Listener<JSONObject>() {
        public void onResponse(JSONObject response) {
            boolean regIdStoredSuccesfully = storeRegistrationId();
            if (regIdStoredSuccesfully) {
                gcmHelperListener.onRegisterSuccess(regId);
            } else {
                gcmHelperListener.onRegisterFailure(regId);
            }

        }
    };

    Response.ErrorListener gcmRequestErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            gcmHelperListener.onRegisterFailure(regId);
        }
    };


    private void registerInBackground(final String userId) {
        /**
         * Creates an AsyncTask that registers the application with GCM servers asynchronously.
         * <p/>
         * Stores the registration ID and the app versionCode in the application's
         * shared preferences if successfully accepted by server.
         *
         */
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return sendRequestToRegisterWithServer(userId, gcmRegisterSuccessListener, gcmRequestErrorListener);
            }

            @Override
            protected void onPostExecute(Boolean requestSent) {
                if (!requestSent){
                    gcmHelperListener.onRequestNotSent(regId);
                } else {
                    gcmHelperListener.onRequestSent(regId);
                }
            }
        }.execute(null, null, null);
    }

    @Deprecated
    public AsyncTask<Void, Void, String> getAsyncTaskSendGCMMessage(final TextView mDisplay) {
        return new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                int a = 4;
                Log.i(LogConstants.LOG_ID, msg);
                mDisplay.append(msg + "\n");
            }
        };
    }




    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId() {
        String registrationId = preferenceAccess.getGcmRegistrationId();

        if (registrationId.isEmpty()) {
            Log.i(LogConstants.LOG_ID, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = preferenceAccess.getAppVersion();
        int currentVersion = AppInfo.getAppVersion(activity);
        if (registeredVersion != currentVersion) {
            Log.i(LogConstants.LOG_ID, "App version changed.");
            return "";
        }
        return registrationId;
    }


    /**
     * @return true if the client sends a request to register
     * @param userId
     * @param gcmRegisterSuccessListener
     * @param gcmRequestErrorListener
     */
    private boolean sendRequestToRegisterWithServer(String userId, Response.Listener<JSONObject> gcmRegisterSuccessListener, Response.ErrorListener gcmRequestErrorListener)  {
        boolean requestSent = false;
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(activity);
        }
        try {
            regId = gcm.register(SENDER_ID);
            requestSent = sendRegistrationIdToBackend(userId, gcmRegisterSuccessListener, gcmRequestErrorListener) &&
                    regId != null && !regId.isEmpty() &&
                    userId != null && !userId.isEmpty();
        } catch (IOException e) {
            Log.e(LogConstants.LOG_ID, "Unable to register with gcm" + e.toString());
            // TODO JS callback
        }

        return requestSent;
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     *
     * @returns true if the request was sent to the server
     * @param userId
     * @param gcmRegisterSuccessListener
     * @param gcmRequestErrorListener
     */
    private boolean sendRegistrationIdToBackend(String userId, Response.Listener<JSONObject> gcmRegisterSuccessListener, Response.ErrorListener gcmRequestErrorListener) {
        Log.i(LogConstants.LOG_ID, "Trying to register to backend");
        return backendServer.registerGCMRegKey(regId, userId, gcmRegisterSuccessListener, gcmRequestErrorListener);
    }


    /**
     * Stores the registration ID and the app versionCode in the application's only if we get
     * a success response from the server.
     * <p/>
     * {@code SharedPreferences}.
     */
    private boolean storeRegistrationId() {
        // TODO JS store the userId as well so that the MainActivity can check that and not query for userId.
        int appVersion = AppInfo.getAppVersion(activity);
        Log.i(LogConstants.LOG_ID, "Saving regId on app version " + appVersion);
        return preferenceAccess.updateGCMRegistrationId(appVersion, regId);
    }


}
