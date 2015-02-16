package com.standapp.activity.common;

/**
 * Created by John on 2/2/2015.
 */

import android.app.Activity;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.standapp.activity.MainActivity;
import com.standapp.activity.SettingsActivity;
import com.standapp.activity.error.ChromeExtErrorActivity;
import com.standapp.activity.error.GenericErrorActivity;
import com.standapp.app.StandAppScopeModule;
import com.standapp.backend.BackendServer;
import com.standapp.backend.UserHelper;
import com.standapp.backend.UserInfoMediator;
import com.standapp.common.ForActivity;
import com.standapp.common.ForApplication;
import com.standapp.fragment.GraphingCardFragment;
import com.standapp.fragment.SuperAwesomeCardFragment;
import com.standapp.google.gcm.GCMHelper;
import com.standapp.google.GooglePlayServicesHelper;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.preferences.PreferenceAccess;
import com.standapp.util.UserInfo;
import com.standapp.util.UserTransformer;

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
                MainActivity.class,
                SuperAwesomeCardFragment.class,
                GraphingCardFragment.class,
                ChromeExtErrorActivity.class,
                SettingsActivity.class,
                GenericErrorActivity.class
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
    GCMHelper provideGCMHelper(PreferenceAccess preferenceAccess, Activity activity, BackendServer backendServer, UserInfo userInfo) {
        return new GCMHelper(preferenceAccess, activity, backendServer, userInfo);
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

    @Provides
    @Singleton
    UserHelper provideUserHelper(BackendServer backendServer, UserInfo userInfo, UserTransformer userTransformer, UserInfoMediator userInfoMediator) {
        return new UserHelper(backendServer, userInfo, userTransformer, userInfoMediator);
    }

    @Provides
    UserTransformer provideUserTransformer() {
        return new UserTransformer();
    }

    @Provides
    GoogleFitAPIHelper provideGoogleFitAPIHelper(@ForApplication Context context, PreferenceAccess preferenceAccess) {
        return new GoogleFitAPIHelper(context, preferenceAccess);
    }

}