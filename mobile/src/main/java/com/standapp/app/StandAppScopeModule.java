package com.standapp.app;

/**
 * Created by John on 2/2/2015.
 */

import com.standapp.annotations.GCMSenderID;
import com.standapp.backend.UserInfoMediator;
import com.standapp.common.AndroidAppModule;
import com.standapp.util.UserInfo;

import javax.inject.Singleton;

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
                StandAppApplication.class
        }
)
public class StandAppScopeModule {

    @Provides
    @GCMSenderID
    String providesGCMSenderID() {
        return "Injected: senderID";
    }

    @Provides
    @Singleton
    UserInfo provideUserInfo(){
        return new UserInfo();
    }

    @Provides
    @Singleton
    UserInfoMediator provideUserInfoMediator() {
        return new UserInfoMediator();
    }

}
