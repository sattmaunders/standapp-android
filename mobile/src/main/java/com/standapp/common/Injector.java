package com.standapp.common;

import dagger.ObjectGraph;

/**
 * Created by John on 2/2/2015.
 */
public interface Injector {
    /**
     * Inject to <code>object</code>
     *
     * @param object
     */
    void inject(Object object);

    ObjectGraph getObjectGraph();
}