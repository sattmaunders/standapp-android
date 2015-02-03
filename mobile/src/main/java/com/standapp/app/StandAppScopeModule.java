package com.standapp.app;

/**
 * Created by John on 2/2/2015.
 */

import android.content.Context;

import com.standapp.MainActivity;
import com.standapp.annotations.GCMSenderID;
import com.standapp.backend.BackendServer;
import com.standapp.common.AndroidAppModule;
import com.standapp.common.ForApplication;
import com.standapp.google.GooglePlayServicesHelper;

import dagger.Module;
import dagger.Provides;

/**
 * Here it provides the dependencies those are used in the whole scope of your StandApp
 */
@Module(
        complete = true,    // Here it enables object graph validation
        library = true,
        addsTo = AndroidAppModule.class, // Important for object graph validation at compile time
        injects = {
                StandAppApplication.class,
                MainActivity.class
        }
)
public class StandAppScopeModule {

    @Provides
    @GCMSenderID
    String providesGCMSenderID() {
        return "Injected: senderID";
    }

    @Provides
    BackendServer provideBackendServer(@ForApplication Context context) {
        BackendServer backendServer = new BackendServer(context);
        return backendServer;
    }

    @Provides
    GooglePlayServicesHelper provideGooglePlayServicesHelper() {
        return new GooglePlayServicesHelper();
    }

//    @Provides
//    RequestQueue provideRequestQueue(@ForApplication Context context) {
//        RequestQueue requestQueue = Volley.newRequestQueue(context);
//        return requestQueue;
//    }



}
