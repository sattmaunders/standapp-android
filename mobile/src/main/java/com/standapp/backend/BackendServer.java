package com.standapp.backend;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

    // TODO JS How can we inject this?
    private RequestQueue requestQueue;

    public BackendServer(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public BackendServer(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    //    Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
//        public void onResponse(JSONObject response) {
//            Log.d(LogConstants.LOG_ID, "Successful response");
//        }
//    };
//
//    Response.ErrorListener errorListener = new Response.ErrorListener() {
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            Log.d(LogConstants.LOG_ID, "Failed response");
//        }
//    };

    public void registerDevice(String registrationId, String userId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        String url = SERVER_BASE_URL + "/" + REST_REGISTER;
        Log.d(LogConstants.LOG_ID, "Registering " + registrationId + " for user " + userId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null, successListener, errorListener);
        requestQueue.add(request);
    }

    public void startWorkout(String userId) {

    }

    public void endWorkout(String userId) {

    }


    public void doSomethingCool() {

        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                Log.d(LogConstants.LOG_ID, "Successful response");
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LogConstants.LOG_ID, "Failed response");
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://ip.jsontest.com/", null, successListener, errorListener);
        requestQueue.add(request);

    }
}
