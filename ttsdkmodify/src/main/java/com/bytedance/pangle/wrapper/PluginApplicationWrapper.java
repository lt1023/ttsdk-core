package com.bytedance.pangle.wrapper;

import android.app.Application;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Bundle;
import com.bytedance.pangle.PluginContext;
import com.bytedance.pangle.util.FieldUtils;
import java.io.File;

/**
 * 跳转应用市场
 */
public class PluginApplicationWrapper extends Application {
    public Application mOriginApplication;
    public PluginContext mPluginContext;

    public PluginApplicationWrapper(Application application, PluginContext pluginContext) {
        this.mOriginApplication = application;
        this.mPluginContext = pluginContext;
        FieldUtils.writeField(this, "mBase", pluginContext);
    }

    @Override // android.app.Application
    public void onCreate() {
        this.mOriginApplication.onCreate();
    }

    @Override // android.app.Application
    public void onTerminate() {
        this.mOriginApplication.onTerminate();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        this.mOriginApplication.onConfigurationChanged(configuration);
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onLowMemory() {
        this.mOriginApplication.onLowMemory();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        this.mOriginApplication.onTrimMemory(i);
    }

    @Override // android.app.Application
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks activityLifecycleCallbacks) {
        this.mOriginApplication.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    @Override // android.app.Application
    public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks activityLifecycleCallbacks) {
        this.mOriginApplication.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    @Override // android.app.Application
    public void registerOnProvideAssistDataListener(Application.OnProvideAssistDataListener onProvideAssistDataListener) {
        this.mOriginApplication.registerOnProvideAssistDataListener(onProvideAssistDataListener);
    }

    @Override // android.app.Application
    public void unregisterOnProvideAssistDataListener(Application.OnProvideAssistDataListener onProvideAssistDataListener) {
        this.mOriginApplication.unregisterOnProvideAssistDataListener(onProvideAssistDataListener);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void setTheme(int i) {
        this.mOriginApplication.setTheme(i);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void startActivity(Intent intent) {
//        this.mOriginApplication.startActivity(intent);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void startActivity(Intent intent, Bundle bundle) {
//        this.mOriginApplication.startActivity(intent, bundle);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void startActivities(Intent[] intentArr) {
        this.mOriginApplication.startActivities(intentArr);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void startActivities(Intent[] intentArr, Bundle bundle) {
        this.mOriginApplication.startActivities(intentArr, bundle);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void startIntentSender(IntentSender intentSender, Intent intent, int i, int i2, int i3) throws IntentSender.SendIntentException {
        this.mOriginApplication.startIntentSender(intentSender, intent, i, i2, i3);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void startIntentSender(IntentSender intentSender, Intent intent, int i, int i2, int i3, Bundle bundle) throws IntentSender.SendIntentException {
        this.mOriginApplication.startIntentSender(intentSender, intent, i, i2, i3, bundle);
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public File getDataDir() {
        return this.mPluginContext.getDataDir();
    }
}