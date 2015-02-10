package com.standapp.google.googlefitapi;

import android.content.Context;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

/**
 * Created by John on 2/9/2015.
 */
public class GoogleFitAPIHelper {

    private final Context context;
    private GoogleApiClient mClient;

    public GoogleFitAPIHelper(Context context) {
        this.context = context;
    }

    public void connect() {
        mClient.connect();
    }

    public boolean isConnected() {
        return mClient.isConnected();
    }


    /**
     * Build a {@link com.google.android.gms.common.api.GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     *
     * TODO JS should this be in constructor?
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

    public void disconnect() {
        mClient.disconnect();
    }

    public boolean isConnecting() {
        return mClient.isConnecting();
    }

    public GoogleApiClient getClient() {
        return mClient;
    }
}
