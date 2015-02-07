package com.standapp.backend;

import org.json.JSONObject;

/**
 * Created by SINTAJ2 on 2/7/2015.
 */
public interface UserHelperListener {
    public void onUserExists(JSONObject userEmail);
    public void onEmailMissing(String userEmail);
    public void onUserNotFound(String userEmail);
}
