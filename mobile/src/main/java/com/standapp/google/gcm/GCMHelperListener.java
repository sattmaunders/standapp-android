package com.standapp.google.gcm;

/**
 * Created by John on 2/4/2015.
 */
public interface GCMHelperListener {

    public void onRegisterSuccess(String regId);
    public void onRegisterFailure(String regId);
    public void onRequestSent(String regId);
    public void onRequestNotSent(String regId);
    public void onAlreadyRegistered(String regId);
}
