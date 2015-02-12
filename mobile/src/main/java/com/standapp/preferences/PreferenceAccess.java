package com.standapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.standapp.activity.MainActivity;

/**
 * Created by John on 2/3/2015.
 *
 * A class to help access the app's preferences to read/write.
 */
public class PreferenceAccess {

    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_LAST_FIT_SESSION_ID = "lastFitSessionId";

    private Context context;

    public PreferenceAccess(Context context) {
        this.context = context;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getSharedPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    public int getAppVersion(){
        return getSharedPreferences().getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    }


    public String getGcmRegistrationId(){
        return getSharedPreferences().getString(PROPERTY_REG_ID, "");
    }

    public boolean updateGCMRegistrationId(int appVersion, String regId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        return editor.commit();
    }

    public String getLastFitSessionId(){
        return getSharedPreferences().getString(PROPERTY_LAST_FIT_SESSION_ID, "");
    }

    public boolean updateLastFitSessionId(String sessionId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PROPERTY_LAST_FIT_SESSION_ID, sessionId);
        return editor.commit();
    }
}
