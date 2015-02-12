package com.standapp.backend;

import com.standapp.util.User;

/**
 * Created by John on 2/12/2015.
 */
public interface UserInfoListener {
    public void onUserUpdated(User user);
    public void onEmailMissing(String userEmail);
    public void onUserNotFound(String userEmail);
}
