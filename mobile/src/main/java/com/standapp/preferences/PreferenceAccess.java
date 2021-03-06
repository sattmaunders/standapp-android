package com.standapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.standapp.activity.SettingsActivity;
import com.standapp.logger.LogConstants;
import com.standapp.util.AppInfo;

/**
 * Created by John on 2/3/2015.
 *
 * A class to help access the app's preferences to read/write.
 */
public class PreferenceAccess {

    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_LAST_FIT_SESSION_ID = "lastFitSessionId";
    private static final String PROPERTY_USER_ACCOUNT = "userAccount";
    private static final String PROPERTY_USER_ID = "userId";
    private static final String PROPERTY_CONFIRM_BREAK = "askForConfirmationBeforeWorkout";
    private static final String PROPERTY_FIRST_TIME_APP_INSTALLED = "firstTimeAppInstalled";

    private Context context;

    public PreferenceAccess(Context context) {
        this.context = context;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
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

    public String getUserAccount(){
        return getSharedPreferences().getString(PROPERTY_USER_ACCOUNT, "");
    }

    public boolean updateUserAccount(String account) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PROPERTY_USER_ACCOUNT, account);
        return editor.commit();
    }

    public String getUserId() {
        return getSharedPreferences().getString(PROPERTY_USER_ID, "");
    }

    public boolean updateUserId(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PROPERTY_USER_ID, userId);
        return editor.commit();
    }

    public boolean clearRegId() {
        int appVersion = AppInfo.getAppVersion(context);
        Log.i(LogConstants.LOG_ID, "Clearing regId on app version " + appVersion);
        return this.updateGCMRegistrationId(appVersion, "");
    }

    public boolean updateStepRecording(boolean b) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(SettingsActivity.pref_key_step_recording, b);
        return editor.commit();
    }

    public void updateConfirmBreak(boolean b) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PROPERTY_CONFIRM_BREAK, b);
        editor.apply();
    }

    public boolean getStepRecording() {
        return getSharedPreferences().getBoolean(SettingsActivity.pref_key_step_recording, false);
    }

    public boolean getSessionRecording() {
        return getSharedPreferences().getBoolean(SettingsActivity.pref_key_session_recording, true);
    }

    public boolean getSound() {
        return getSharedPreferences().getBoolean(SettingsActivity.pref_key_sound, true);
    }

    public boolean getVibrate() {
        return getSharedPreferences().getBoolean(SettingsActivity.pref_key_vibrate, true);
    }

    public boolean getLight() {
        return getSharedPreferences().getBoolean(SettingsActivity.pref_key_light, false);
    }

    public boolean isStartWorkoutNotificationEnabled() {
        return getSharedPreferences().getBoolean(SettingsActivity.pref_key_start_workout_notification, true)
                && !isConfirmBreak();
    }

    public boolean isConfirmBreak() {
        return getSharedPreferences().getBoolean(PROPERTY_CONFIRM_BREAK, false);
    }

    public boolean isFirstTime() {
        return getSharedPreferences().getBoolean(PROPERTY_FIRST_TIME_APP_INSTALLED, true);
    }

    public void updateFirstTime(boolean b) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PROPERTY_FIRST_TIME_APP_INSTALLED, b);
        editor.apply();
    }
}
