package com.standapp.backend;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.standapp.util.User;
import com.standapp.util.UserInfo;
import com.standapp.util.UserTransformer;

import org.json.JSONObject;

/**
 * Created by SINTAJ2 on 2/7/2015.
 */
public class UserHelper {

    private BackendServer backendServer;
    private UserInfo userInfo;
    private UserTransformer userTransformer;
    private UserInfoMediator userInfoMediator;

    public UserHelper(BackendServer backendServer, UserInfo userInfo, UserTransformer userTransformer, UserInfoMediator userInfoMediator) {
        this.backendServer = backendServer;
        this.userInfo = userInfo;
        this.userTransformer = userTransformer;
        this.userInfoMediator = userInfoMediator;
    }

    public void getUserInfo() {
        updateUser(userInfo.getUserEmail());
    }

    /**
     * Updates the {@link com.standapp.util.UserInfo} with user data and notifies listener
     * that the user data is ready to party!
     *
     */
    public void updateUser(final String userEmail) {

        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                //Convert response to User object
                User user = userTransformer.buildUserObject(response);
                userInfo.setUser(user);
                userInfoMediator.notifyUserInfoListenersUserInfoUpdated(user);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 400) {
                    userInfoMediator.notifyUserInfoListenersEmailMissing(userEmail);
                    // TODO JS throw exception b/c no email was created and report
                } else if (error.networkResponse.statusCode == 404) {
                    userInfoMediator.notifyUserInfoListenersUserNotFound(userEmail);
                }
            }
        };

        backendServer.getUserByEmail(userEmail, successListener, errorListener);

    }

}
