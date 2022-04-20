package com.linktower.application;

import android.content.Context;


public class Application extends android.app.Application {
    private static Context context;

    static {
        System.loadLibrary("linktower");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Application.context = getApplicationContext();
        GameSDK.onCreate(this);
    }

    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        GameSDK.init(context);
    }

    public static Context getAppContext() {
        return Application.context;
    }
}

