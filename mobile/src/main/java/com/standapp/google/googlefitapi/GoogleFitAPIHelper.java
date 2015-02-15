package com.standapp.google.googlefitapi;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.standapp.R;
import com.standapp.logger.LogConstants;
import com.standapp.preferences.PreferenceAccess;

/**
 * Created by John on 2/9/2015.
 */
public class GoogleFitAPIHelper {

    private final Context context;
    private GoogleApiClient mClient;
    private PreferenceAccess preferenceAccess;

    public GoogleFitAPIHelper(Context context, PreferenceAccess preferenceAccess) {
        this.context = context;
        this.preferenceAccess = preferenceAccess;
    }

    public void connect() {
        mClient.connect();
    }

    /**
     * Build a {@link com.google.android.gms.common.api.GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     * <p/>
     * TODO JS should this be in constructor?
     *
     * @param connectionCallbacks
     * @param onConnectionFailedListener
     */
    public void buildFitnessClient(GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        mClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
    }


    public boolean isConnected() {
        return mClient.isConnected();
    }

    public void unregisterListeners(GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener){
        if (connectionCallbacks != null){
            mClient.unregisterConnectionCallbacks(connectionCallbacks);
        }

        if (onConnectionFailedListener != null){
            mClient.unregisterConnectionFailedListener(onConnectionFailedListener);
        }

    }

    public void disconnect() {
        mClient.disconnect();
    }

    public boolean isConnecting() {
        return mClient.isConnecting();
    }

    // TODO JS refactor any calls to this into this class
    public GoogleApiClient getClient() {
        return mClient;
    }

    /**
     * This revokes OAuth connect with the app and also clears our user related preferences
     *
     * @param activity in which to display toast messages of results.
     */
    public void revokeFitPermissions(final Activity activity, final RevokeGoogleFitPermissionsListener revokePermissionsListener) {
        if (mClient != null && mClient.isConnected()) {
            PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(mClient);
            pendingResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    Log.i(LogConstants.LOG_ID, "Disconnect fit " + status.toString() + ", code " + status.getStatus().getStatusCode());
                    if (status.isSuccess()) {
                        Toast.makeText(activity, activity.getString(R.string.toast_googlefit_disconnect_success), Toast.LENGTH_LONG).show();
                        preferenceAccess.updateUserAccount("");
                        preferenceAccess.updateUserId("");
                        preferenceAccess.clearRegId();
                        revokePermissionsListener.onRevokedFitPermissions();
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.toast_googlefit_disconnect_failed), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(activity, activity.getString(R.string.toast_googlefit_disconnect_failed_notconnected), Toast.LENGTH_LONG).show();
        }

    }
}
