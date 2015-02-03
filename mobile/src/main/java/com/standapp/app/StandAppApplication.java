package com.standapp.app;

import com.standapp.common.DaggerApplication;

import java.util.Collections;
import java.util.List;

/**
 * Created by John on 2/2/2015.
 */
public class StandAppApplication extends DaggerApplication {
    @Override
    protected List<Object> getAppModules() {
        return Collections.<Object>singletonList(new StandAppScopeModule());
    }
}
