package com.standapp.activity;

/**
 * Created by John on 2/2/2015.
 */

import android.app.Activity;
import android.content.Context;

import com.standapp.app.StandAppScopeModule;
import com.standapp.common.ForActivity;

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
}