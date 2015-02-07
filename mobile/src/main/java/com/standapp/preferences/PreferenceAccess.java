package com.standapp.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.standapp.activity.MainActivity;

/**
 * Created by John on 2/3/2015.
 *
 * A class to help access the app's preferences to read/write.
 */
public class PreferenceAccess {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private Activity activity;

    public PreferenceAccess(Activity activity) {
        this.activity = activity;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return activity.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    public String getGcmRegistrationId(){
        return getGcmPreferences().getString(PROPERTY_REG_ID, "");
    }

    public int getAppVersion(){
        return getGcmPreferences().getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    }


    public boolean updateGCMRegistrationId(int appVersion, String regId) {
        SharedPreferences.Editor editor = getGcmPreferences().edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        return editor.commit();
    }
}
