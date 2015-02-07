package com.standapp.backend;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.standapp.util.UserInfo;

import org.json.JSONObject;

/**
 * Created by SINTAJ2 on 2/7/2015.
 */
public class UserHelper {

    private BackendServer backendServer;
    private UserInfo userInfo;

    public UserHelper(BackendServer backendServer, UserInfo userInfo) {
        this.backendServer = backendServer;
        this.userInfo = userInfo;
    }

    public void checkIfUserIsCreated(final UserHelperListener userHelperListener) {
        checkIfUserIsCreated(userInfo.getUserEmail(), userHelperListener);
    }

    public void checkIfUserIsCreated(final String userEmail, final UserHelperListener userHelperListener) {

        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                userHelperListener.onUserExists(response);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 400) {
                    userHelperListener.onEmailMissing(userEmail);
                    // TODO JS throw exception b/c no email was created and report
                } else if (error.networkResponse.statusCode == 404) {
                    userHelperListener.onUserNotFound(userEmail);
                }
            }
        };

        backendServer.getUserByEmail(userEmail, successListener, errorListener);

    }
}
