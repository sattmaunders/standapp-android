package com.standapp.activity;

/**
 * Created by John on 2/2/2015.
 */

import android.app.Activity;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.standapp.MainActivity;
import com.standapp.app.StandAppScopeModule;
import com.standapp.backend.BackendServer;
import com.standapp.common.ForActivity;
import com.standapp.common.ForApplication;
import com.standapp.google.GCMHelper;
import com.standapp.google.GooglePlayServicesHelper;
import com.standapp.preferences.PreferenceAccess;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Here it provides the dependencies those have same lifetime of one activity in your StandApp
 */
@Module(
        complete = true,    // Here we enable object graph validation
        library = true,
        addsTo = StandAppScopeModule.class, // Important for object graph validation at compile time
        injects = {
                MainActivity.class
        }
)
public class ActivityScopeModule {

    private final Activity mActivity;

    public ActivityScopeModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    @Singleton
    @ForActivity
    Context provideActivityContext() {
        return mActivity;
    }

    @Provides
    @Singleton
    Activity provideActivity() {
        return mActivity;
    }

    @Provides
    GCMHelper provideGCMHelper(PreferenceAccess preferenceAccess, Activity activity, BackendServer backendServer) {
        return new GCMHelper(preferenceAccess, activity, backendServer);
    }

    @Provides
    @Singleton
    BackendServer provideBackendServer(RequestQueue requestQueue) {
        BackendServer backendServer = new BackendServer(requestQueue);
        return backendServer;
    }

    @Provides
    @Singleton
    GooglePlayServicesHelper provideGooglePlayServicesHelper() {
        return new GooglePlayServicesHelper();
    }

    @Provides
    RequestQueue provideRequestQueue(@ForApplication Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        return requestQueue;
    }

    @Provides
    @Singleton
    PreferenceAccess providePreferenceAccess(Activity activity) {
        PreferenceAccess pa = new PreferenceAccess(activity);
        return pa;
    }







}