package com.standapp.activity.common;

import com.standapp.common.BasePreferenceActivity;

/**
 * Created by John on 2/2/2015.
 */
public abstract class StandAppBasePreferenceActivity extends BasePreferenceActivity {

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new ActivityScopeModule(this),
                // new AnotherCoolActivityScopedModule(),
        };
    }
}
