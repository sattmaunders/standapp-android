package com.standapp.backend;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.standapp.logger.LogConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by John on 2/1/2015.
 * <p/>
 * An easy way to talk to our NodeJS server which usually deals with GCM related stuff.
 */
public class BackendServer {

    public static final String SERVER_BASE_URL = "http://standapp-server.herokuapp.com";

    public static final String GCM_FIELD_SENDER_ID = "senderId";
    public static final String GCM_FIELD_MESSAGE_KEY = "messageKey";

    private static final String REST_USER = "user";
    private static final String REST_GCMKEY = "gcmKey";

    private static final String REST_WORKOUT_START = "workout/start";
    private static final String REST_WORKOUT_END = "workout/end";
    private static final String REST_MESSAGE = "message";
    public static final String USER_ID = "userId";
    public static final String REG_ID = "regId";
    public static final String EMAIL = "email";

    private RequestQueue requestQueue;

    public BackendServer(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    /**
     * Ask server for user by email. If no user exists, then we need them to download the ChromeExt.
     *
     * @param userEmail
     * @param successListener
     * @param errorListener
     * @return
     */
    public boolean getUserByEmail(String userEmail, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        String url = SERVER_BASE_URL + "/" + REST_USER + "?" + EMAIL + "=" + userEmail;
        Log.d(LogConstants.LOG_ID, "getUserByEmail: " + userEmail);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, successListener, errorListener);
        requestQueue.add(request);
        return true;
    }


    public boolean registerGCMRegKey(String registrationId, String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        String url = SERVER_BASE_URL + "/" + REST_USER + "/" + userId + "/" + REST_GCMKEY + "/" + registrationId;
        Log.d(LogConstants.LOG_ID, "Registering " + registrationId + " for userId " + userId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null, successListener, errorListener);
        requestQueue.add(request);
        return true;
    }

    public boolean endBreak(String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        String url = SERVER_BASE_URL + "/" + REST_USER + "/" + userId + "/" + REST_MESSAGE;
        Log.d(LogConstants.LOG_ID, "End breaking for userId " + userId);
        JSONObject contentJSON = new JSONObject();
        JSONObject messageJSON = new JSONObject();
        try {
            messageJSON.put(GCM_FIELD_MESSAGE_KEY, StandAppMessages.BREAK_END.toString());
            messageJSON.put(GCM_FIELD_SENDER_ID, SenderId.PHONE.toString());
            contentJSON.put("content", messageJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, contentJSON, successListener, errorListener);
        requestQueue.add(request);
        return true;
    }

    public void startWorkout(String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {

    }

    public void endWorkout(String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {

    }
}
