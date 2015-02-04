package com.standapp.google.gcm;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.standapp.backend.BackendServer;
import com.standapp.util.AppInfo;
import com.standapp.logger.Log;
import com.standapp.logger.LogConstants;
import com.standapp.preferences.PreferenceAccess;
import com.standapp.util.UserInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by John on 2/3/2015.
 */
public class GCMHelper {

//    Activity activity = null;
    public static final String EXTRA_MESSAGE = "message";


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleCloudMessaging gcm;
    private String regid;

    private PreferenceAccess preferenceAccess;
    private Activity activity;
    private BackendServer backendServer;
    private UserInfo userInfo;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    private String SENDER_ID = "665143645608";
    private AtomicInteger msgId = new AtomicInteger();

    public GCMHelper(PreferenceAccess preferenceAccess, Activity activity, BackendServer backendServer, UserInfo userInfo) {
        this.preferenceAccess = preferenceAccess;
        this.activity = activity;
        this.backendServer = backendServer;
        this.userInfo = userInfo;
    }

    public void registerDevice(final TextView display) {

        gcm = GoogleCloudMessaging.getInstance(activity);
        regid = getRegistrationId();

        if (regid.isEmpty()) {
            registerInBackground(display);
        } else {
            Log.i(LogConstants.LOG_ID, "Device already registered with " + regid);
        }

    }

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
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences if successfully accepted by server.
     * @param display
     */
    private void registerInBackground(final TextView display) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(activity);
                    }
                    regid = gcm.register(SENDER_ID);

                    sendRegistrationIdToBackend(regid);
                    msg = "Trying to register to backend";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // TODO JS If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                display.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     * @param regid
     */
    private void sendRegistrationIdToBackend(final String regid) {

        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                Log.d(LogConstants.LOG_ID, "Device registered, registration ID=" + regid);
                storeRegistrationId(activity, regid);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LogConstants.LOG_ID, "Failed registered " + regid + " to backend, unable to persist data");
                // TODO JS Throw exception and re-try?
            }
        };


        backendServer.registerDevice(regid, userInfo.getUserId(), successListener, errorListener);
    }


    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        int appVersion = AppInfo.getAppVersion(context);
        Log.i(LogConstants.LOG_ID, "Saving regId on app version " + appVersion);
        preferenceAccess.updateGCMRegistrationId(appVersion, regId);
    }


}
