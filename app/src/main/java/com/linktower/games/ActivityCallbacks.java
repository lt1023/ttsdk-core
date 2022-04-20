package com.linktower.games;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;



public class ActivityCallbacks implements Application.ActivityLifecycleCallbacks {

    @SuppressLint("StaticFieldLeak")
    private static ActivityCallbacks instance;
    private static Context NewContext;

    public static ActivityCallbacks init(Application application){
        if (instance == null) {
            instance = new ActivityCallbacks();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
    }

    public static Context getNewContext() {
        return NewContext;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if(activity.getParent()!=null){
            NewContext = activity.getParent();
        }else
            NewContext = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if(activity.getParent()!=null){
            NewContext = activity.getParent();
        }else
            NewContext = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(activity.getParent()!=null){
            NewContext = activity.getParent();
        }else
            NewContext = activity;
    }

    @Override
    public void onActivityPaused( Activity activity) {

    }

    @Override
    public void onActivityStopped( Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState( Activity activity,  Bundle outState) {

    }

    @Override
    public void onActivityDestroyed( Activity activity) {

    }
}
