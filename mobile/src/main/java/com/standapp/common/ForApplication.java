package com.standapp.common;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
/**
 * Created by John on 2/2/2015.
 */
@Qualifier
@Retention(RUNTIME)
public @interface ForApplication {
}
