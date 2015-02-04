package com.standapp.backend;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.standapp.logger.Log;
import com.standapp.logger.LogConstants;

import org.json.JSONObject;

/**
 * Created by John on 2/1/2015.
 * <p/>
 * An easy way to talk to our NodeJS server which usually deals with GCM related stuff.
 */
public class BackendServer {

    public static final String SERVER_BASE_URL = "http://standapp-2015.herokuapp.com";

    private static final String REST_REGISTER = "register";
    private static final String REST_WORKOUT_START = "workout/start";
    private static final String REST_WORKOUT_END = "workout/end";

    private RequestQueue requestQueue;

    public BackendServer(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }


    public void registerDevice(String registrationId, String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        String url = SERVER_BASE_URL + "/" + REST_REGISTER;
        Log.d(LogConstants.LOG_ID, "Registering " + registrationId + " for user " + userId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null, successListener, errorListener);
        requestQueue.add(request);
    }

    public void startWorkout(String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {

    }

    public void endWorkout(String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {

    }
}
