package com.standapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.standapp.activity.MainActivity;
import com.standapp.logger.Log;

import org.json.JSONObject;

public class LockScreenActivity extends ActionBarActivity {

    @Override
    protected void onResume() {
        super.onResume();
        lockScreenViaHTTP();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
    }

    private void lockScreenViaHTTP() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = MainActivity.SERVER_BASE_URL + "/lock/away";
        Log.i(MainActivity.TAG, "locking screen");

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        Log.i(MainActivity.TAG, "Succes to lock screen via HTTP request");
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(MainActivity.TAG, "Failed to lock screen via HTTP request");
            }
        });
        queue.add(stringRequest);
    }


}
