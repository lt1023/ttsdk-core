package com.linktower.games;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.umeng.commonsdk.UMConfigure;

//import de.robv.android.xposed.DexposedBridge;
//import de.robv.android.xposed.XC_MethodHook;
//import de.robv.android.xposed.XC_MethodReplacement;


//import de.robv.android.xposed.DexposedBridge;
//import de.robv.android.xposed.XC_MethodHook;
//import de.robv.android.xposed.XC_MethodReplacement;


/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class TTAdManagerHolder {

    private static final String TAG = "TTAdManagerHolder";
    private static boolean sInit;
    public static TTAdManager get() {
        return TTAdSdk.getAdManager();
    }
    public static void init(final Context context) {
        doInit(context);
    }

    //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
    private static void doInit(Context context) {
        ActivityCallbacks.init((Application) context);
        if (!sInit) {
            Logger.setDebug(true);
            Config.init(context);
            //TTAdSdk.init(context, buildConfig(context));
            TTAdSdk.init(context, buildConfig(context), new TTAdSdk.InitCallback() {
                @Override
                public void success() {
                    Logger.d("success: "+ TTAdSdk.isInitSuccess());
                }

                @Override
                public void fail(int code, String msg) {
                    Logger.d("init fail:  code = " + code + " msg = " + msg);
                }
            });
            sInit = true;
            UMConfigure.init(context, Config.getConfig().getUmeng_id(), "233", UMConfigure.DEVICE_TYPE_PHONE,null);
        }
//        init();
    }

    private static TTAdConfig buildConfig(Context context) {
        Log.w("初始化","进入初始化");
        return new TTAdConfig.Builder()
                .appId(Config.getConfig().getApp_id())
                .useTextureView(false) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                .allowShowNotify(false) //是否允许sdk展示通知栏提示
                .debug(false) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_3G, TTAdConstant.NETWORK_STATE_4G,
                        TTAdConstant.NETWORK_STATE_2G, TTAdConstant.NETWORK_STATE_5G, TTAdConstant.NETWORK_STATE_MOBILE) //允许直接下载的网络状态集合
                .supportMultiProcess(false)//是否支持多进程
                .needClearTaskReset()
                .build();
    }

//    public static void init() {
//        try {
////            Log.d("SDK_INT", Build.VERSION.SDK_INT + "");
//            if (Build.VERSION.SDK_INT > 31){
//                return;
//            }
//            DexposedBridge.hookAllMethods(Activity.class, "onResume", new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    Logger.d("onResume : " + param.thisObject);
//                }
//            });
//
//            DexposedBridge.hookAllMethods(ContextWrapper.class, "startActivity", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
////                DexposedBridge.log("startActivity");
//                    try {
//                        Intent intent = (Intent) param.args[0];
//                        Uri uri = intent.getData();
//                        if (uri == null)return;
//                        String string = uri.toString();
////                    DexposedBridge.log(string);
//                        if (string.startsWith("market://details?")){
//                            param.args[0] = "";
//                        }
//                        throw new NullPointerException("");
//                    }catch (Exception e){
////                    DexposedBridge.log(e);
//                    }
//                }
//            });
//
//            Class AppDownloadUtils = null;
//            try {
//                AppDownloadUtils =  Class.forName("com.ss.android.socialbase.appdownloader.c");
//            } catch (ClassNotFoundException ignore) {
//            }
//
//            if (AppDownloadUtils != null){
//                DexposedBridge.findAndHookMethod(AppDownloadUtils, "a", Context.class, Intent.class,
//                        new XC_MethodReplacement() {
//                            @Override
//                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
//                                try {
//                                    if (LinkSDK.getInstance().isInstall()){
//                                        DexposedBridge.invokeOriginalMethod(param.method, param.thisObject, new Object[]{param.args[0], param.args[1]});
//                                    }
//                                }catch (Exception e){
//                                    Logger.e("" , e);
//                                }
//                                return 0;
//                            }
//                        });
//            }
//
//            DexposedBridge.hookAllMethods(Toast.class, "show", new XC_MethodReplacement() {
//                @Override
//                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
//                    return null;
//                }
//            });
//
//            Class DebuggerA = null;
//            try {
//                DebuggerA = Class.forName("com.applovin.impl.mediation.debugger.a");
//            } catch (ClassNotFoundException ignore) {}
//
//            DexposedBridge.log(DebuggerA+"");
//            if (DebuggerA != null){
//                DexposedBridge.findAndHookMethod(DebuggerA, "a", List.class, new XC_MethodReplacement() {
//                    @Override
//                    protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                        return null;
//                    }
//                });
//            }
//        }catch (Exception ignore){}
//    }
}

