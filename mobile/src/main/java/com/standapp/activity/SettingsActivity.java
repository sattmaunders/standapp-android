package com.standapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
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
    public static final String pref_key_active_subscriptions = "pref_key_active_subscriptions";

    public static final String pref_key_start_workout_notification = "pref_key_start_workout_notification";
    public static final String pref_key_vibrate = "pref_key_vibrate";
    public static final String pref_key_sound = "pref_key_sound";
    public static final String pref_key_light = "pref_key_light";


    // MAKE ENUMS TODO
    private static final int REVOKE_GOOGLE_FIT_PERMISSIONS = 0;
    private static final int UNSUBSCRIBE_STEP_DATA = 1;
    private static final int SUBSCRIBE_STEP_DATA = 2;
    private static final int TRACK_SESSION_ON = 3;
    private static final int TRACK_SESSION_OFF = 4;
    private static final int VIEW_ACTIVE_SUBSCRIPTIONS = 5;

    private int typeOfWork = -1;


    // Track how many async calls were successful, i.e for async subscribing
    private int numAsyncSuccessfullCallbacks = 0;


    public static final DataType[] SUBSCRIBED_DATA_TYPES = new DataType[] {DataType.TYPE_STEP_COUNT_CUMULATIVE, DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_LOCATION_SAMPLE};

    @Inject
    GoogleFitAPIHelper googleFitAPIHelper;

    @Inject
    PreferenceAccess preferenceAccess;

    private Preference prefViewActiveSubscriptions;
    private Preference prefDisconnectFit = null;
    private Preference prefWorkoutNotifications = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        prefDisconnectFit = (Preference) findPreference(pref_key_disconnect_fit);
        prefViewActiveSubscriptions = (Preference) findPreference(pref_key_active_subscriptions);
        prefWorkoutNotifications = (Preference) findPreference(pref_key_start_workout_notification);
        setClickListeners();

        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
        refreshPrefDisconnectFitSummary();
        refreshPrefWorkoutNotifications();
    }

    private void refreshPrefWorkoutNotifications() {
        boolean confirmBreak = preferenceAccess.isConfirmBreak();
        if (confirmBreak) {
            prefWorkoutNotifications.setSummary(getString(R.string.pref_summary_start_workout_warning));
        }
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
        prefDisconnectFit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (googleFitAPIHelper.isConnected()) {
                    revokeGoogleFitPermissions();
                } else {
                    setTypeOfWork(REVOKE_GOOGLE_FIT_PERMISSIONS);
                    googleFitAPIHelper.connect();
                }
                return true;
            }
        });

        prefViewActiveSubscriptions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (googleFitAPIHelper.isConnected()) {
                    viewActiveSubscriptions();
                } else {
                    setTypeOfWork(VIEW_ACTIVE_SUBSCRIPTIONS);
                    googleFitAPIHelper.connect();
                }
                return true;
            }

        });
    }


    private void viewActiveSubscriptions() {
        PendingResult<ListSubscriptionsResult> res = googleFitAPIHelper.listActiveSubscriptions();
        res.setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
            @Override
            public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                String res = "";
                for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                    DataType dt = sc.getDataType();
                    if (dt.equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)) {
                        res += SettingsActivity.this.getString(R.string.data_type_display_step_count_cumulative) + "<br>";
                    } else if (dt.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                        res += SettingsActivity.this.getString(R.string.data_type_display_step_count_delta) + "<br>";
                    } else if (dt.equals(DataType.TYPE_LOCATION_SAMPLE)) {
                        res += SettingsActivity.this.getString(R.string.data_type_display_location_sample) + "<br>";
                    } else {
                        res += dt.getName() + "<br>";
                    }
                }


                if (res.isEmpty()){
                    res = SettingsActivity.this.getString(R.string.no_active_subs);
                } else {
                    // Add disclaimer
                    res += "<br>";
                    res += "<small>" + SettingsActivity.this.getString(R.string.dialog_active_subs_disclaimer) + "</small>";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle(R.string.dialog_title_active_subscriptions);
                builder.setMessage(Html.fromHtml(res));
                builder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
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
                subscribeToStepsAndLocation();
            } else if (typeOfWork == UNSUBSCRIBE_STEP_DATA) {
                unsubscribeToStepsAndLocation();
            } else if (typeOfWork == VIEW_ACTIVE_SUBSCRIPTIONS) {
                viewActiveSubscriptions();
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
        if (key.equals(pref_key_step_recording)) {

            boolean subscribeSteps = sharedPreferences.getBoolean(key, false);

            if (subscribeSteps) {
                if (googleFitAPIHelper.isConnected()) {
                    subscribeToStepsAndLocation();
                } else {
                    setTypeOfWork(SUBSCRIBE_STEP_DATA);
                    googleFitAPIHelper.connect();
                }
            } else {
                if (googleFitAPIHelper.isConnected()) {
                    unsubscribeToStepsAndLocation();
                } else {
                    setTypeOfWork(UNSUBSCRIBE_STEP_DATA);
                    googleFitAPIHelper.connect();
                }
            }
        }
    }

    private void subscribeToStepsAndLocation() {
        numAsyncSuccessfullCallbacks = 0;
        for (DataType dataType : SUBSCRIBED_DATA_TYPES){
            subscribe(dataType);
        }
    }

    private void unsubscribeToStepsAndLocation() {
        numAsyncSuccessfullCallbacks = 0;
        for (DataType dataType : SUBSCRIBED_DATA_TYPES){
            unsubscribe(dataType);
        }
    }

    private void unsubscribe(final DataType dataType) {
        PendingResult<Status> unsubscribe = googleFitAPIHelper.unsubscribe(dataType);
        unsubscribe.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                String msg = "";
                if (status.isSuccess()) {
                    msg = "Successfully unsubscribed for data type: " + dataType.getName();
                    numAsyncSuccessfullCallbacks++;
                } else {
                    msg = "Failed to unsubscribe for data type: " + dataType.getName();
                }
                Log.i(LogConstants.LOG_ID, msg);
                if (numAsyncSuccessfullCallbacks == SUBSCRIBED_DATA_TYPES.length) {
                    Toast.makeText(SettingsActivity.this, SettingsActivity.this.getString(R.string.toast_unsubscribed_succes), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    private void toast(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//        Log.i(LogConstants.LOG_ID, msg);
//    }


    private void subscribe(final DataType dataType) {
        PendingResult<Status> res = googleFitAPIHelper.subscribe(dataType);
        res.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                String msg = "";
                if (status.isSuccess()) {
                    numAsyncSuccessfullCallbacks++;
                    if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                        msg = "Already sub to " + dataType.getName();
                    } else {
                        msg = "Successfully subscribed! " + dataType.getName();
                    }
                } else {
                    msg = "There was a problem subscribing." + dataType.getName();
                }
                Log.i(LogConstants.LOG_ID, msg);
                if (numAsyncSuccessfullCallbacks == SUBSCRIBED_DATA_TYPES.length) {
                    Toast.makeText(SettingsActivity.this, SettingsActivity.this.getString(R.string.toast_subscribed_succes), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

}
