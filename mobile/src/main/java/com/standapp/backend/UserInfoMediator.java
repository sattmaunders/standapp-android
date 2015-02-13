package com.standapp.backend;

import com.standapp.util.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2/12/2015.
 */
public class UserInfoMediator {

    private List<UserInfoListener> userInfoListenerList;

    public UserInfoMediator() {
        this.userInfoListenerList = new ArrayList<UserInfoListener>();
    }

    public void registerUserInfoListener(UserInfoListener userInfoListener){
        if (!userInfoListenerList.contains(userInfoListener)){
            userInfoListenerList.add(userInfoListener);
        }
    }

    public void unregisterUserInfoListener(UserInfoListener userInfoListener){
        userInfoListenerList.remove(userInfoListener);
    }

    public void notifyUserInfoListenersUserInfoUpdated(User user){
        for (UserInfoListener userInfoListener : userInfoListenerList){
            userInfoListener.onUserRefreshed(user);
        }
    }

    public void notifyUserInfoListenersEmailMissing(String userEmail){
        for (UserInfoListener userInfoListener : userInfoListenerList){
            userInfoListener.onEmailMissing(userEmail);
        }
    }

    public void notifyUserInfoListenersUserNotFound(String userEmail){
        for (UserInfoListener userInfoListener : userInfoListenerList){
            userInfoListener.onUserNotFound(userEmail);
        }
    }

}
