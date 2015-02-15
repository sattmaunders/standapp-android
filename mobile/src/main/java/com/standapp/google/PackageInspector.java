package com.standapp.google;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by SINTAJ2 on 2/15/2015.
 */
public class PackageInspector {

    public static final String GOOGLE_FIT_APP_PACKAGE = "com.google.android.apps.fitness";

    private Context context;

    public PackageInspector(Context context) {
        this.context = context;
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
