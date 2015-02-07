package com.standapp.activity.common;

import com.standapp.common.BaseActionBarActivity;

/**
 * Created by John on 2/2/2015.
 */
public abstract class StandAppBaseActionBarActivity extends BaseActionBarActivity {

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new ActivityScopeModule(this),
                // new AnotherCoolActivityScopedModule(),
        };
    }
}
