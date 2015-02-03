package com.standapp.activity;

import com.standapp.common.BaseActivity;

/**
 * Created by John on 2/2/2015.
 */
public abstract class StandAppBaseActivity extends BaseActivity {

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new ActivityScopeModule(this),
                // new AnotherCoolActivityScopedModule(),
        };
    }
}
