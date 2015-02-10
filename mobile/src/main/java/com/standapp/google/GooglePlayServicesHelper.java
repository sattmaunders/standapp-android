package com.standapp.google;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.standapp.logger.LogConstants;

/**
 * Created by John on 2/3/2015.
 */
public class GooglePlayServicesHelper {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices(Activity activity) {
        int resultCode = com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (com.google.android.gms.common.GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LogConstants.LOG_ID, "This device is not supported.");
                if (activity != null){
                    activity.finish();
                }
            }
            return false;
        }
        return true;
    }
}
