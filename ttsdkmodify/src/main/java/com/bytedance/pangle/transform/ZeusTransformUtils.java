package com.bytedance.pangle.transform;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.pangle.ComponentManager;
import com.bytedance.pangle.PluginContext;
import com.bytedance.pangle.Zeus;
import com.bytedance.pangle.activity.GeneratePluginActivity;
import com.bytedance.pangle.activity.GeneratePluginAppCompatActivity;
import com.bytedance.pangle.activity.GenerateProxyActivity;
import com.bytedance.pangle.activity.GenerateProxyAppCompatActivity;
import com.bytedance.pangle.activity.IPluginActivity;
import com.bytedance.pangle.log.ZeusLogger;
import com.bytedance.pangle.plugin.PluginManager;
import com.bytedance.pangle.receiver.PluginBroadcastReceiver;
import com.bytedance.pangle.res.PluginResources;
import com.bytedance.pangle.service.client.ServiceManagerNative;
import com.bytedance.pangle.util.FieldUtils;
import com.bytedance.pangle.util.MethodUtils;
import com.bytedance.pangle.wrapper.PluginActivityWrapper;
import com.bytedance.pangle.wrapper.PluginApplicationWrapper;
import com.bytedance.pangle.wrapper.PluginFragmentActivityWrapper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import org.xmlpull.v1.XmlPullParser;


/**
 * 跳转安装startActivity
 */
public class ZeusTransformUtils {
    private static final String TAG = "PluginContextUtils";
    public static Class fragmentClazz;
    static HashMap<String, WeakReference<Context>> contextCache = new HashMap<>();
    static boolean hasEnsure = false;
    static HashMap<String, Constructor<View>> sConstructorMap = null;

    public static int mapRes(int i, String str, String str2) {
        if (i < 2130706432) {
            return i;
        }
        int identifier = Zeus.getAppApplication().getResources().getIdentifier(str2, str, Zeus.getAppApplication().getPackageName());
        if (identifier == 0) {
            identifier = Zeus.getAppApplication().getResources().getIdentifier(str2.replace("_", "."), str, Zeus.getAppApplication().getPackageName());
        }
        if (identifier != 0) {
            return identifier;
        }
        throw new RuntimeException("error" + str2 + " " + i);
    }

    public static Object wrapperContextForParams(Object obj, Class cls, String str) {
        if (!(obj instanceof GeneratePluginActivity) && !(obj instanceof GeneratePluginAppCompatActivity) && (obj instanceof Context)) {
            Context wrapperContext = wrapperContext(obj, str);
            if (cls.isInstance(wrapperContext(obj, str))) {
                return wrapperContext;
            }
        }
        return obj;
    }

    public static boolean instanceOf(Object obj, Class cls) {
        Object obj2;
        if (obj instanceof PluginContext) {
            return cls.isInstance(((PluginContext) obj).mOriginContext);
        }
        if (obj instanceof PluginActivityWrapper) {
            return cls.isInstance(((PluginActivityWrapper) obj).mOriginActivity);
        }
        if (obj instanceof PluginFragmentActivityWrapper) {
            try {
                try {
                    obj2 = ((PluginFragmentActivityWrapper) obj).getOriginActivity();
                } catch (Throwable th) {
                    throw new RuntimeException(th);
                }
            } catch (Throwable unused) {
                obj2 = FieldUtils.readField(obj, "mOriginActivity");
            }
            return cls.isInstance(obj2);
        } else if (obj instanceof PluginApplicationWrapper) {
            return cls.isInstance(((PluginApplicationWrapper) obj).mOriginApplication);
        } else {
            return cls.isInstance(obj);
        }
    }

    public static Application wrapperContext2Application(Object obj, String str) {
        while (obj != null) {
            Context wrapperContext = wrapperContext(obj, str);
            if (wrapperContext instanceof Application) {
                return (Application) wrapperContext;
            }
            if (wrapperContext instanceof PluginContext) {
                obj = ((PluginContext) wrapperContext).mOriginContext;
            } else {
                throw new RuntimeException("强转失败");
            }
        }
        return null;
    }

    public static Activity wrapperContext2Activity(Object obj, String str) {
        while (obj != null) {
            Context wrapperContext = wrapperContext(obj, str);
            if (wrapperContext instanceof Activity) {
                return (Activity) wrapperContext;
            }
            if (wrapperContext instanceof PluginContext) {
                obj = ((PluginContext) wrapperContext).mOriginContext;
            } else {
                throw new RuntimeException("强转失败");
            }
        }
        return null;
    }

    public static Object preCheckCast(Object obj, Class cls, String str) {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Context)) {
            return obj;
        }
        boolean z = !cls.isInstance(obj);
        if (equalsFragmentActivity(cls)) {
            return wrapperContext2FragmentActivity(obj, str);
        }
        if (cls == Activity.class) {
            return wrapperContext2Activity(obj, str);
        }
        if (cls == Application.class) {
            return wrapperContext2Application(obj, str);
        }
        if ((obj instanceof PluginContext) && z) {
            return ((PluginContext) obj).mOriginContext;
        }
        if ((obj instanceof PluginFragmentActivityWrapper) && z) {
            try {
                try {
                    return ((PluginFragmentActivityWrapper) obj).getOriginActivity();
                } catch (Throwable unused) {
                    return FieldUtils.readField(obj, "mOriginActivity");
                }
            } catch (Throwable th) {
                throw new RuntimeException(th);
            }
        } else if (!(obj instanceof PluginActivityWrapper) || !z) {
            return (!(obj instanceof PluginApplicationWrapper) || !z) ? obj : ((PluginApplicationWrapper) obj).mOriginApplication;
        } else {
            return ((PluginActivityWrapper) obj).mOriginActivity;
        }
    }

    private static Object wrapperContext2FragmentActivity(Object obj, String str) {
        while (obj != null) {
            Context wrapperContext = wrapperContext(obj, str);
            if (instanceOfFragmentActivity(wrapperContext)) {
                return wrapperContext;
            }
            if (wrapperContext instanceof PluginContext) {
                obj = ((PluginContext) wrapperContext).mOriginContext;
            } else {
                throw new RuntimeException("强转失败");
            }
        }
        return null;
    }

    public static Context wrapperContext(Object obj, String str) {
        Context context;
        Application application;
        if (Zeus.getAppApplication() == null && (application = (Application) ((Context) obj).getApplicationContext()) != null) {
            Zeus.setAppContext(application);
        }
        if (obj == null) {
            return null;
        }
        Context wrapperFromCache = getWrapperFromCache(obj, str);
        if (wrapperFromCache != null) {
            return wrapperFromCache;
        }
        Context context2 = (Context) obj;
        Context contextIfNeedWrap = getContextIfNeedWrap(context2, context2, str);
        if (contextIfNeedWrap == null) {
            return context2;
        }
        if (instanceOfFragmentActivity(contextIfNeedWrap)) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            try {
                try {
                    context = new PluginFragmentActivityWrapper((Activity) contextIfNeedWrap, new PluginContext(contextIfNeedWrap, PluginManager.getInstance().getPlugin(str), false));
                } catch (Throwable unused) {
                    context = (Context) MethodUtils.invokeConstructor(PluginFragmentActivityWrapper.class, new Object[]{contextIfNeedWrap, new PluginContext(contextIfNeedWrap, PluginManager.getInstance().getPlugin(str), false)}, new Class[]{fragmentClazz, PluginContext.class});
                }
            } catch (Throwable unused2) {
                return contextIfNeedWrap;
            }
        } else if (contextIfNeedWrap instanceof Activity) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            context = new PluginActivityWrapper((Activity) contextIfNeedWrap, new PluginContext(contextIfNeedWrap, PluginManager.getInstance().getPlugin(str), false));
        } else if (contextIfNeedWrap instanceof Application) {
            context = new PluginApplicationWrapper((Application) contextIfNeedWrap, new PluginContext(contextIfNeedWrap, PluginManager.getInstance().getPlugin(str), true));
        } else {
            context = new PluginContext(contextIfNeedWrap, PluginManager.getInstance().getPlugin(str), false);
        }
        if (context != null) {
            HashMap<String, WeakReference<Context>> hashMap = contextCache;
            hashMap.put(str + System.identityHashCode(contextIfNeedWrap), new WeakReference<>(context));
        }
        return context;
    }

    private static Context getWrapperFromCache(Object obj, String str) {
        HashMap<String, WeakReference<Context>> hashMap = contextCache;
        WeakReference<Context> weakReference = hashMap.get(str + System.identityHashCode(obj));
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    public static Resources getResources(Object obj, String str) {
        try {
            Resources resources = (Resources) MethodUtils.invokeMethod(obj, "getResources", new Object[0]);
            if (resources == null) {
                return null;
            }
            if (resources instanceof PluginResources) {
                String str2 = (String) FieldUtils.readField(resources, "pluginPkg");
                if (TextUtils.isEmpty(str2)) {
                    String assetPaths = getAssetPaths(resources.getAssets());
                    if (assetPaths.contains("/" + str + "/version")) {
                        return resources;
                    }
                } else if (TextUtils.equals(str2, str)) {
                    return resources;
                }
            }
            return PluginManager.getInstance().getPlugin(str).mResources;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Context getContextIfNeedWrap(Context context, Context context2, String str) {
        Object obj;
        while (context2 != null) {
            if (context2 instanceof PluginContext) {
                PluginContext pluginContext = (PluginContext) context2;
                if (TextUtils.equals(pluginContext.mPlugin.mPkgName, str)) {
                    return null;
                }
                return pluginContext.mOriginContext;
            } else if (context2 instanceof PluginActivityWrapper) {
                PluginActivityWrapper pluginActivityWrapper = (PluginActivityWrapper) context2;
                if (TextUtils.equals(pluginActivityWrapper.pluginContext.mPlugin.mPkgName, str)) {
                    return null;
                }
                return pluginActivityWrapper.mOriginActivity;
            } else if (context2 instanceof PluginFragmentActivityWrapper) {
                if (TextUtils.equals(((PluginFragmentActivityWrapper) context2).pluginContext.mPlugin.mPkgName, str)) {
                    return null;
                }
                try {
                    try {
                        obj = ((PluginFragmentActivityWrapper) context2).getOriginActivity();
                    } catch (Throwable th) {
                        throw new RuntimeException(th);
                    }
                } catch (Throwable unused) {
                    obj = FieldUtils.readField(context2, "mOriginActivity");
                }
                return (Context) obj;
            } else if (context2 instanceof PluginApplicationWrapper) {
                PluginApplicationWrapper pluginApplicationWrapper = (PluginApplicationWrapper) context2;
                if (TextUtils.equals(pluginApplicationWrapper.mPluginContext.mPlugin.mPkgName, str)) {
                    return null;
                }
                return pluginApplicationWrapper.mOriginApplication;
            } else if (context2.getResources() instanceof PluginResources) {
                PluginResources pluginResources = (PluginResources) context2.getResources();
                try {
                    String str2 = (String) FieldUtils.readField(pluginResources, "pluginPkg");
                    if (TextUtils.isEmpty(str2)) {
                        String assetPaths = getAssetPaths(pluginResources.getAssets());
                        if (!assetPaths.contains("/" + str + "/version")) {
                            return context;
                        }
                    } else if (!TextUtils.equals(str2, str)) {
                        return context;
                    }
                } catch (Throwable th2) {
                    th2.printStackTrace();
                }
                return null;
            } else if (!(context2 instanceof ContextWrapper)) {
                return context;
            } else {
                try {
                    context2 = (Context) FieldUtils.readField(context2, "mBase");
                } catch (Throwable unused2) {
                    context2 = ((ContextWrapper) context2).getBaseContext();
                }
            }
        }
        return null;
    }

    public static View inflate(LayoutInflater layoutInflater, int i, ViewGroup viewGroup, boolean z, String str) {
        Context context = layoutInflater.getContext();
        if (!(context instanceof PluginContext) && !(context instanceof PluginActivityWrapper) && !(context instanceof PluginFragmentActivityWrapper) && !(context instanceof PluginApplicationWrapper)) {
            layoutInflater = (LayoutInflater) wrapperContext(context, str).getSystemService("layout_inflater");
        }
        clearConstructorCache();
        View inflate = layoutInflater.inflate(i, viewGroup, z);
        clearConstructorCache();
        return inflate;
    }

    public static View inflate(LayoutInflater layoutInflater, int i, ViewGroup viewGroup, String str) {
        return inflate(layoutInflater, i, viewGroup, viewGroup != null, str);
    }

    public static View inflate(LayoutInflater layoutInflater, XmlPullParser xmlPullParser, ViewGroup viewGroup, String str) {
        return inflate(layoutInflater, xmlPullParser, viewGroup, viewGroup != null, str);
    }

    public static View inflate(LayoutInflater layoutInflater, XmlPullParser xmlPullParser, ViewGroup viewGroup, boolean z, String str) {
        Context context = layoutInflater.getContext();
        if (!(context instanceof PluginContext) && !(context instanceof PluginActivityWrapper) && !(context instanceof PluginFragmentActivityWrapper) && !(context instanceof PluginApplicationWrapper)) {
            layoutInflater = (LayoutInflater) wrapperContext(context, str).getSystemService("layout_inflater");
        }
        clearConstructorCache();
        View inflate = layoutInflater.inflate(xmlPullParser, viewGroup, z);
        clearConstructorCache();
        return inflate;
    }

    public static View inflate(Context context, int i, ViewGroup viewGroup, String str) {
        if (!(context instanceof PluginContext) && !(context instanceof PluginActivityWrapper) && !(context instanceof PluginFragmentActivityWrapper) && !(context instanceof PluginApplicationWrapper)) {
            context = wrapperContext(context, str);
        }
        clearConstructorCache();
        View inflate = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(i, viewGroup);
        clearConstructorCache();
        return inflate;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Activity _getActivity(Object obj, String str) {
        Activity activity = (Activity) MethodUtils.invokeMethod(obj, "getActivity", new Object[0]);
        Activity activity2 = (Activity) wrapperContext(activity, str);
        if (activity2 instanceof GenerateProxyActivity) {
            return ((GenerateProxyActivity) activity2).mTargetActivity;
        }
        if (activity2 instanceof GenerateProxyAppCompatActivity) {
            return ((GenerateProxyAppCompatActivity) activity2).mTargetActivity;
        }
        return (Activity) wrapperContext(activity, str);
    }

    public static Activity getActivity(Object obj, String str) {
        return _getActivity(obj, str);
    }

    public static Context getContext(Object obj, String str) {
        try {
            Context context = (Context) MethodUtils.invokeMethod(obj, "getContext", new Object[0]);
            if (instanceOfFragmentActivity(context)) {
                return wrapperContext(context, str);
            }
            if (context instanceof Activity) {
                return wrapperContext(context, str);
            }
            if (context instanceof Application) {
                return wrapperContext(context, str);
            }
            if (context instanceof PluginContext) {
                return context;
            }
            return wrapperContext(context, str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Intent registerReceiver(Object obj, PluginBroadcastReceiver pluginBroadcastReceiver, IntentFilter intentFilter, String str) {
        if (obj instanceof Context) {
            ZeusLogger.d(ZeusLogger.TAG_RECEIVER, "ZeusTransformUtils-registerReceiver-execute");
            return ComponentManager.registerReceiver((Context) obj, pluginBroadcastReceiver, intentFilter, str);
        }
        try {
            return (Intent) MethodUtils.invokeMethod(obj, "registerReceiver", pluginBroadcastReceiver, intentFilter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Intent registerReceiver(Object obj, PluginBroadcastReceiver pluginBroadcastReceiver, IntentFilter intentFilter, String str, Handler handler, String str2) {
        if (obj instanceof Context) {
            ZeusLogger.d(ZeusLogger.TAG_RECEIVER, "ZeusTransformUtils-registerReceiver-execute[4 params]");
            return ComponentManager.registerReceiver((Context) obj, pluginBroadcastReceiver, intentFilter, str, handler, str2);
        }
        try {
            return (Intent) MethodUtils.invokeMethod(obj, "registerReceiver", pluginBroadcastReceiver, intentFilter, str, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Intent registerReceiver(Object obj, PluginBroadcastReceiver pluginBroadcastReceiver, IntentFilter intentFilter, int i, String str) {
        if (obj instanceof Context) {
            ZeusLogger.d(ZeusLogger.TAG_RECEIVER, "ZeusTransformUtils-registerReceiver-execute[3 params]");
            return ComponentManager.registerReceiver((Context) obj, pluginBroadcastReceiver, intentFilter, i, str);
        }
        try {
            return (Intent) MethodUtils.invokeMethod(obj, "registerReceiver", pluginBroadcastReceiver, intentFilter, Integer.valueOf(i));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Intent registerReceiver(Object obj, PluginBroadcastReceiver pluginBroadcastReceiver, IntentFilter intentFilter, String str, Handler handler, int i, String str2) {
        if (obj instanceof Context) {
            ZeusLogger.d(ZeusLogger.TAG_RECEIVER, "ZeusTransformUtils-registerReceiver-execute[5 params]");
            return ComponentManager.registerReceiver((Context) obj, pluginBroadcastReceiver, intentFilter, str, handler, i, str2);
        }
        try {
            return (Intent) MethodUtils.invokeMethod(obj, "registerReceiver", pluginBroadcastReceiver, intentFilter, str, handler, Integer.valueOf(i));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unregisterReceiver(Object obj, PluginBroadcastReceiver pluginBroadcastReceiver, String str) {
        if (obj instanceof Context) {
            ComponentManager.unregisterReceiver((Context) obj, pluginBroadcastReceiver);
            return;
        }
        try {
            MethodUtils.invokeMethod(obj, "unregisterReceiver", pluginBroadcastReceiver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void startActivity(Object obj, Intent intent, String str) {
        startActivity(obj, intent, null, str);
    }

    private static long timestamps = 0;

    public static void startActivity(Object obj, Intent intent, Bundle bundle, String str) {

        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - timestamps < 1000 * 60 * 5) {
                timestamps = currentTime;
                return;
            }
        } catch (Exception ignore) {
        }

        try {
            ComponentManager.startActivity(obj, intent, bundle, str);
        } catch (Throwable th) {
            if (obj instanceof Context) {
                ComponentManager.startActivity((Context) obj, intent, bundle, str);
                return;
            }
            try {
                MethodUtils.invokeMethod(obj, "startActivity", new Object[]{intent, bundle}, new Class[]{Intent.class, Bundle.class});
            } catch (Throwable th2) {
                th2.addSuppressed(th);
                throw new RuntimeException(th2);
            }
        }
    }

    public static void startActivityForResult(Object obj, Intent intent, int i, String str) {
        startActivityForResult(obj, intent, i, null, str);
    }

    public static void startActivityForResult(Object obj, Intent intent, int i, Bundle bundle, String str) {
//        try {
//            ComponentManager.startActivityForResult(obj, intent, i, bundle, str);
//        } catch (Throwable th) {
//            if (obj instanceof Activity) {
//                ComponentManager.startActivityForResult((Activity) obj, intent, i, bundle, str);
//                return;
//            }
//            try {
//                MethodUtils.invokeMethod(obj, "startActivityForResult", new Object[]{intent, Integer.valueOf(i), bundle}, new Class[]{Intent.class, Integer.TYPE, Bundle.class});
//            } catch (Throwable th2) {
//                th2.addSuppressed(th);
//                throw new RuntimeException(th2);
//            }
//        }
    }

    public static Class forName(String str, String str2) throws ClassNotFoundException {
        return Zeus.getPlugin(str2).mClassLoader.loadClass(str);
    }

    public static void setResult(Object obj, int i, Intent intent, String str) {
        if (obj instanceof Activity) {
            try {
                Object readField = FieldUtils.readField(obj, "mProxyActivity");
                if (readField != null) {
                    MethodUtils.invokeMethod(readField, "setResult", Integer.valueOf(i), intent);
                    return;
                }
            } catch (Exception unused) {
                ZeusLogger.d(ZeusLogger.TAG_ACTIVITY, "Cant find mProxyActivity, obj = ".concat(String.valueOf(obj)));
            }
        }
        try {
            MethodUtils.invokeMethod(obj, "setResult", Integer.valueOf(i), intent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setResult(Object obj, int i, String str) {
        if (obj instanceof Activity) {
            try {
                Object readField = FieldUtils.readField(obj, "mProxyActivity");
                if (readField != null) {
                    MethodUtils.invokeMethod(readField, "setResult", Integer.valueOf(i));
                    return;
                }
            } catch (Exception unused) {
                ZeusLogger.d(ZeusLogger.TAG_ACTIVITY, "Cant find mProxyActivity, obj = ".concat(String.valueOf(obj)));
            }
        }
        try {
            MethodUtils.invokeMethod(obj, "setResult", Integer.valueOf(i));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ComponentName startService(Object obj, Intent intent, String str) {
        if (obj instanceof Context) {
            return ServiceManagerNative.getInstance().startServiceNative((Context) obj, intent, str);
        }
        try {
            return (ComponentName) MethodUtils.invokeMethod(obj, "startService", intent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean stopService(Object obj, Intent intent, String str) {
        if (obj instanceof Context) {
            return ServiceManagerNative.getInstance().stopServiceNative((Context) obj, intent, str);
        }
        try {
            return ((Boolean) MethodUtils.invokeMethod(obj, "stopService", intent)).booleanValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean bindService(Object obj, Intent intent, ServiceConnection serviceConnection, int i, String str) {
        if (obj instanceof Context) {
            return ServiceManagerNative.getInstance().bindServiceNative((Context) obj, intent, serviceConnection, i, str);
        }
        try {
            return ((Boolean) MethodUtils.invokeMethod(obj, "bindService", new Object[]{intent, serviceConnection, Integer.valueOf(i)}, new Class[]{Intent.class, ServiceConnection.class, Integer.TYPE})).booleanValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unbindService(Object obj, ServiceConnection serviceConnection, String str) {
        if (obj instanceof Context) {
            ServiceManagerNative.getInstance().unbindServiceNative(serviceConnection);
            return;
        }
        try {
            MethodUtils.invokeMethod(obj, "unbindService", serviceConnection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void requestPermissions(Object obj, String[] strArr, int i, String str) {
        if (obj instanceof IPluginActivity) {
            ((IPluginActivity) obj)._requestPermissions(strArr, i);
            return;
        }
        try {
            MethodUtils.invokeMethod(obj, "requestPermissions", strArr, Integer.valueOf(i));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getIdentifier(Object obj, String str, String str2, String str3, String str4) {
        if (obj instanceof Resources) {
            Resources resources = (Resources) obj;
            if (!TextUtils.equals("android", str3)) {
                str3 = str4;
            }
            return resources.getIdentifier(str, str2, str3);
        }
        try {
            return ((Integer) MethodUtils.invokeMethod(obj, "getIdentifier", str, str2, str3)).intValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerZeusActivityStub(String str, String[] strArr, String str2) {
        ComponentManager.registerActivity(str2, str, strArr);
    }

    private static void ensureFragmentActivity() {
        if (!hasEnsure) {
            try {
                try {
                    fragmentClazz = Zeus.class.getClassLoader().loadClass("android.support.v4.app.FragmentActivity");
                } catch (Throwable unused) {
                }
            } catch (Throwable unused2) {
                try {
                    fragmentClazz = Zeus.class.getClassLoader().loadClass("androidx.fragment.app.FragmentActivity");
                } catch (Exception ignore) {
                }
            }
            hasEnsure = true;
        }
    }

    private static boolean instanceOfFragmentActivity(Object obj) {
        ensureFragmentActivity();
        Class cls = fragmentClazz;
        if (cls == null) {
            return false;
        }
        return cls.isInstance(obj);
    }

    private static boolean equalsFragmentActivity(Class cls) {
        ensureFragmentActivity();
        Class cls2 = fragmentClazz;
        return cls2 != null && cls == cls2;
    }

    public static void clearConstructorCache() {
        if (Build.VERSION.SDK_INT <= 23) {
            try {
                if (sConstructorMap == null) {
                    sConstructorMap = (HashMap) FieldUtils.readStaticField(LayoutInflater.class, "sConstructorMap");
                }
                for (String str : sConstructorMap.keySet()) {
                    if (!str.startsWith("android.view.") && !str.startsWith("android.widget.") && !str.startsWith("android.webkit.") && str.contains(".")) {
                        sConstructorMap.remove(str);
                    }
                }
            } catch (Throwable unused) {
            }
        }
    }

    public static String getAssetPaths(AssetManager assetManager) {
        StringBuilder sb = new StringBuilder();
        if (assetManager == null) {
            return "";
        }
        try {
        } catch (Throwable th) {
            ZeusLogger.e(ZeusLogger.TAG_RESOURCES, "GetAssetsPaths error. ", th);
            th.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 28 && (Build.VERSION.SDK_INT != 27 || Build.VERSION.PREVIEW_SDK_INT <= 0)) {
            int intValue = ((Integer) MethodUtils.invokeMethod(assetManager, "getStringBlockCount", new Object[0])).intValue();
            int i = 0;
            while (i < intValue) {
                i++;
                String str = (String) MethodUtils.invokeMethod(assetManager, "getCookieName", Integer.valueOf(i));
                if (!TextUtils.isEmpty(str)) {
                    sb.append(str);
                }
            }
            return sb.toString();
        }
        Object[] objArr = (Object[]) MethodUtils.invokeMethod(assetManager, "getApkAssets", new Object[0]);
        if (objArr != null && objArr.length > 0) {
            for (Object obj : objArr) {
                sb.append((String) MethodUtils.invokeMethod(obj, "getAssetPath", new Object[0]));
            }
        }
        return sb.toString();
    }
}