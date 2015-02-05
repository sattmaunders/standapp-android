package com.standapp.backend;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.standapp.logger.Log;
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

    private static final String REST_USER = "user";
    private static final String REST_WORKOUT_START = "workout/start";
    private static final String REST_WORKOUT_END = "workout/end";
    public static final String USER_ID = "userId";
    public static final String REG_ID = "regId";

    private RequestQueue requestQueue;

    public BackendServer(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }


    public boolean registerDevice(String registrationId, String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        String url = SERVER_BASE_URL + "/" + REST_USER;
        Log.d(LogConstants.LOG_ID, "Registering " + registrationId + " for user " + userId);
        JSONObject params = new JSONObject();
        try {
            params.put(USER_ID, userId);
            params.put(REG_ID, registrationId);
        } catch (JSONException e) {
            Log.e(LogConstants.LOG_ID, "Problem creating params for registering user");
            return false;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params, successListener, errorListener);
        requestQueue.add(request);

        return true;
    }

    public void startWorkout(String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {

    }

    public void endWorkout(String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {

    }
}
