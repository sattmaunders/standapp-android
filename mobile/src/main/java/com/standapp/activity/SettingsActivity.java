package com.standapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.standapp.R;
import com.standapp.activity.common.StandAppBasePreferenceActivity;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.google.googlefitapi.RevokeGoogleFitPermissionsListener;
import com.standapp.logger.LogConstants;
import com.standapp.preferences.PreferenceAccess;

import javax.inject.Inject;

public class SettingsActivity extends StandAppBasePreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, RevokeGoogleFitPermissionsListener {


    public static final String pref_key_step_recording = "pref_key_step_recording";
    public static final String pref_key_session_recording = "pref_key_session_recording";
    public static final String pref_key_disconnect_fit = "pref_key_disconnect_fit";


    private static final int REVOKE_GOOGLE_FIT_PERMISSIONS = 0;
    private static final int UNSUBSCRIBE_STEP_DATA = 1;
    private static final int SUBSCRIBE_STEP_DATA = 2;
    private static final int TRACK_SESSION_ON = 3;
    private static final int TRACK_SESSION_OFF = 4;

    private int typeOfWork = -1;


    private Preference prefDisconnectFit = null;


    @Inject
    GoogleFitAPIHelper googleFitAPIHelper;

    @Inject
    PreferenceAccess preferenceAccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        setClickListeners();

        prefDisconnectFit = (Preference) findPreference(pref_key_disconnect_fit);

        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
        refreshPrefDisconnectFitSummary();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleFitAPIHelper.isConnected()) {
            googleFitAPIHelper.disconnect();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleFitAPIHelper.unregisterListeners(connectionCallbacks, onConnectionFailedListener);
    }


    private void refreshPrefDisconnectFitSummary() {
        String userAccount = preferenceAccess.getUserAccount();
        if (userAccount.isEmpty()) {
            prefDisconnectFit.setSummary(getString(R.string.pref_summary_disconnect_fit_no_user));
        } else {
            prefDisconnectFit.setSummary(getString(R.string.pref_summary_disconnect_fit, userAccount));
        }
    }

    private void setClickListeners() {
        prefDisconnectFit = (Preference) findPreference(pref_key_disconnect_fit);

        prefDisconnectFit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (googleFitAPIHelper.isConnected()){
                    revokeGoogleFitPermissions();
                } else {
                    setTypeOfWork(REVOKE_GOOGLE_FIT_PERMISSIONS);
                    googleFitAPIHelper.connect();
                }
                return true;
            }
        });
    }

    public void setTypeOfWork(int typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");

            if (typeOfWork == REVOKE_GOOGLE_FIT_PERMISSIONS) {
                revokeGoogleFitPermissions();
            } else if (typeOfWork == SUBSCRIBE_STEP_DATA) {

            } else if (typeOfWork == UNSUBSCRIBE_STEP_DATA) {

            } else if (typeOfWork == TRACK_SESSION_ON) {

            } else if (typeOfWork == TRACK_SESSION_OFF) {

            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                Log.w(LogConstants.LOG_ID, "Connection lost.  Cause: Network Lost.");
            } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                Log.w(LogConstants.LOG_ID, "Connection lost.  Reason: Service Disconnected");
            }
        }
    };

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.w(LogConstants.LOG_ID, "onConnectionFailed");
        }
    };


    private boolean revokeGoogleFitPermissions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_google_fit_revoke_permissions_title);
        builder.setMessage(getString(R.string.confirm_google_fit_revoke_permissions));
        builder.setIcon(R.drawable.sa_ic_fit);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                googleFitAPIHelper.revokeFitPermissions(SettingsActivity.this, SettingsActivity.this);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(pref_key_session_recording)) {
            Toast.makeText(this, "pref_key_session_recording", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onRevokedFitPermissions() {
        refreshPrefDisconnectFitSummary();
    }

//
//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//
//        setupSimplePreferencesScreen();
//    }
//
//    /**
//     * Shows the simplified settings UI if the device configuration if the
//     * device configuration dictates that a simplified, single-pane UI should be
//     * shown.
//     */
//    private void setupSimplePreferencesScreen() {
//
//        // In the simplified UI, fragments are not used at all and we instead
//        // use the older PreferenceActivity APIs.
//
//        // Add 'general' preferences.
//        addPreferencesFromResource(R.xml.pref_general);
//
//        // Add 'notifications' preferences, and a corresponding header.
//        PreferenceCategory fakeHeader = new PreferenceCategory(this);
//        fakeHeader.setTitle(R.string.pref_header_notifications);
//        getPreferenceScreen().addPreference(fakeHeader);
//        addPreferencesFromResource(R.xml.pref_notification);
//
//        // Add 'data and sync' preferences, and a corresponding header.
//        fakeHeader = new PreferenceCategory(this);
//        fakeHeader.setTitle(R.string.pref_header_data_sync);
//        getPreferenceScreen().addPreference(fakeHeader);
//        addPreferencesFromResource(R.xml.pref_data_sync);
//
//        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
//        // their values. When their values change, their summaries are updated
//        // to reflect the new value, per the Android Design guidelines.
//        bindPreferenceSummaryToValue(findPreference("example_text"));
//        bindPreferenceSummaryToValue(findPreference("example_list"));
//        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
//        bindPreferenceSummaryToValue(findPreference("sync_frequency"));
//    }
//
//
//
//
//    /**
//     * A preference value change listener that updates the preference's summary
//     * to reflect its new value.
//     */
//    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
//        @Override
//        public boolean onPreferenceChange(Preference preference, Object value) {
//            String stringValue = value.toString();
//
//            if (preference instanceof ListPreference) {
//                // For list preferences, look up the correct display value in
//                // the preference's 'entries' list.
//                ListPreference listPreference = (ListPreference) preference;
//                int index = listPreference.findIndexOfValue(stringValue);
//
//                // Set the summary to reflect the new value.
//                preference.setSummary(
//                        index >= 0
//                                ? listPreference.getEntries()[index]
//                                : null);
//
//            } else if (preference instanceof RingtonePreference) {
//                // For ringtone preferences, look up the correct display value
//                // using RingtoneManager.
//                if (TextUtils.isEmpty(stringValue)) {
//                    // Empty values correspond to 'silent' (no ringtone).
//                    preference.setSummary(R.string.pref_ringtone_silent);
//
//                } else {
//                    Ringtone ringtone = RingtoneManager.getRingtone(
//                            preference.getContext(), Uri.parse(stringValue));
//
//                    if (ringtone == null) {
//                        // Clear the summary if there was a lookup error.
//                        preference.setSummary(null);
//                    } else {
//                        // Set the summary to reflect the new ringtone display
//                        // name.
//                        String name = ringtone.getTitle(preference.getContext());
//                        preference.setSummary(name);
//                    }
//                }
//
//            } else {
//                // For all other preferences, set the summary to the value's
//                // simple string representation.
//                preference.setSummary(stringValue);
//            }
//            return true;
//        }
//    };
//
//    /**
//     * Binds a preference's summary to its value. More specifically, when the
//     * preference's value is changed, its summary (line of text below the
//     * preference title) is updated to reflect the value. The summary is also
//     * immediately updated upon calling this method. The exact display format is
//     * dependent on the type of preference.
//     *
//     * @see #sBindPreferenceSummaryToValueListener
//     */
//    private static void bindPreferenceSummaryToValue(Preference preference) {
//        // Set the listener to watch for value changes.
//        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
//
//        // Trigger the listener immediately with the preference's
//        // current value.
//        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
//                PreferenceManager
//                        .getDefaultSharedPreferences(preference.getContext())
//                        .getString(preference.getKey(), ""));
//    }

}
