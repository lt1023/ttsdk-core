package com.linktower.games;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.linktower.games.callback.BannerCallBack;
import com.linktower.games.callback.InterAdCallBack;
import com.linktower.games.callback.RewardedCallBack;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LinkSDK {
    private static LinkSDK instance;

    private LinkSDK() {
    }

    public static LinkSDK getInstance() {
        if (instance == null) {
            instance = new LinkSDK();
        }
        return instance;
    }

    private static final int TYPE_BANNER = 0;//banner广告
    private static final int TYPE_INTER = 1;//插屏广告
    private static final int TYPE_FULLSCREEN = 2;//全屏广告
    private static final int TYPE_REWARDED = 3;//激励视频
    private static final int TYPE_START = 1000;//开始任务
    private static final int TYPE_END = 1001;//结束任务
    private static int TIME_DELAY_OF_AD = 55 * 1000;//广告间隔时长
    private static int INTERAD_ERR_TIMES = 0;//插屏广告错误次数
    private static int FULLSCREEN_ERR_TIMES = 0;//全屏视频广告错误次数
    private static int REWARDED_ERR_TIMES = 0;//全屏视频广告错误次数
    private static int currentAdTimes = 0;//当前静默广告数
    private final static int LIMIT_AD_TIMES = 50;//静默广告总数阈值

    private final ADController mADController = new ADController();

    private boolean isRewardedVisibility = true;
    private boolean isInterAdVisibility = true;
    private static Banner mBanner;
    private static FullScreenAd mFullScreenAd;
    private static NewInterstitial mInterAd;
    private static RewardAd mRewardAd;
    private Activity mGameActivity;

    private int index = 0;
    private SDKSharedPreferences mSharedPreferences;
    private final String DATEFORMAT = "yyyy-MM-dd";
    private String date;
    private TYPE mTYPE;


    public TYPE getAdType(){
        return mTYPE;
    }

    private void setTYPE(TYPE type){
        mTYPE = type;
    }

    /**
     * 增加今日静默广告数
     * @param times
     */
    private void addTodayAdTimes(int times){
        mSharedPreferences.addTodayAdTimes(date, times);
    }

    /**
     * 今日广告限制次数
     *
     * @return
     */
    private boolean isTodayLimited() {
        int todayAdTimes = mSharedPreferences.getTodayAdTimes(date);
        Logger.d("todayAdTimes = " + todayAdTimes);
        if (todayAdTimes > LIMIT_AD_TIMES) {
            return true;
        }
        return false;
    }

    private int install_index = 0;//下载完成调用安装次数
    /**
     * 设置debug模式，可查看调试日志
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        Logger.setDebug(debug);
    }

    /**
     * 是否执行安装
     *
     * @return
     */
    public boolean isInstall() {
        install_index++;
        return install_index % 5 == 0;
    }

    /**
     * 检测网络连接
     *
     * @param context
     */
    public void checkNetWork(Context context) {
        if (!isNetworkConnected(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("系统提示")
                    .setMessage("游戏资源下载错误，请连接网络后重试")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).create().show();
        }
    }

    /**
     * 网络是否可用
     *
     * @param context
     * @return
     */
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    /**
     * 激励视频是否可见
     *
     * @return
     */
    public boolean isRewardedVisibility() {
        Logger.d("isRewardedVisibility " + isRewardedVisibility);
        return isRewardedVisibility;
    }

    /**
     * 设置激励视频是否可见
     */
    public void setRewardedVisibility(boolean visibility) {
        isRewardedVisibility = visibility;
    }

    /**
     * 插屏/全屏视频视频是否可见
     *
     * @return
     */
    public boolean isInterAdVisibility() {
        Logger.d("isInterAdVisibility " + isInterAdVisibility);
        return isInterAdVisibility;
    }

    /**
     * 设置插屏/全屏视频视频是否可见
     */
    public void setInterAdVisibility(boolean visibility) {
        isInterAdVisibility = visibility;
        if (!visibility) {
            if (mGameActivity == null) {
                throw new NullPointerException("please invoke init method first !");
            }
            int volume = closeVolume(mGameActivity);
            Logger.d("volume = " + volume);
        }
    }

    public void init(Activity activity) {
        mGameActivity = activity;
        mBanner = new Banner(activity);
        mFullScreenAd = new FullScreenAd(activity);
        mInterAd = new NewInterstitial(activity);
        mRewardAd = new RewardAd(activity);
        mSharedPreferences = SDKSharedPreferences.getInstance().init(activity);
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);// 可以方便地修改日期格式
        date = dateFormat.format(now);
        Report.getInstance().report(activity);
    }

    /**
     * 广告控制类
     */
    private static class ADController extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            getInstance().setInterAdVisibility(false);
            switch (msg.what) {
                case TYPE_BANNER:
//                    getInstance().showBanner();
                    break;
                case TYPE_INTER:
                    getInstance().showInterAd();
                    break;
                case TYPE_FULLSCREEN:
                    getInstance().showFullScreenAd();
                    break;
                case TYPE_REWARDED:
                    getInstance().showRewardedAd(false);
                    break;
                case TYPE_START:
                    Logger.d("START");
                    if (getInstance().isForeground(getInstance().mGameActivity)) {
                        Logger.d("isForeground");
                        sendEmptyMessage(TYPE_BANNER);
                        int nextInt = new Random().nextInt(100);
                        if (nextInt < 55) {
                            sendEmptyMessage(TYPE_INTER);
                        } else if (nextInt < 90) {
                            sendEmptyMessage(TYPE_FULLSCREEN);
                        } else {
                            sendEmptyMessage(TYPE_REWARDED);
                        }
                        getInstance().addTodayAdTimes(1);
                    }
                    if (getInstance().isTodayLimited()) {
                        sendEmptyMessage(TYPE_END);
                        return;
                    }
                    sendEmptyMessageDelayed(TYPE_START, TIME_DELAY_OF_AD);
                    break;
                case TYPE_END:
                    removeCallbacksAndMessages(null);
            }
        }
    }

    public enum TYPE {
        TYPE_INTERAD, TYPE_FULLSCREEN, TYPE_REWARDED
    }

    /**
     * 广告加载失败回调：加载失败次数过多，则减慢广告播放速度
     *
     * @param type 广告类型
     */
    public void onAdError(TYPE type) {
        switch (type) {
            case TYPE_INTERAD:
                INTERAD_ERR_TIMES++;
                break;
            case TYPE_REWARDED:
                REWARDED_ERR_TIMES++;
                break;
            case TYPE_FULLSCREEN:
                FULLSCREEN_ERR_TIMES++;
        }
    }

    /**
     * 播放banner
     */
    public void showBanner() {
        Logger.d("showBanner");
        mBanner.loadAd();
    }

    /**
     * 设置banner回调
     *
     * @param callBack
     */
    public void setBannerCallBack(BannerCallBack callBack) {
        mBanner.addCallBack(callBack);
    }


    /**
     * 播放插屏视频（新插屏）
     */
    public void showInterAd() {
        Logger.d("showInterAd");
        if (INTERAD_ERR_TIMES > 3) {
            INTERAD_ERR_TIMES = 0;
            TIME_DELAY_OF_AD += 1000 * 60;
        }
        mInterAd.loadAd();
    }

    /**
     * 设置插屏视频回调
     *
     * @param callBack
     */
    public void setInterAdCallBack(InterAdCallBack callBack) {
        mInterAd.addCallBack(callBack);
    }

    /**
     * 播放全屏视频
     */
    public void showFullScreenAd() {
        Logger.d("showFullScreenAd");
        if (FULLSCREEN_ERR_TIMES > 3) {
            FULLSCREEN_ERR_TIMES = 0;
            TIME_DELAY_OF_AD += 1000 * 60;
        }
        mFullScreenAd.loadAd();
    }

    /**
     * 设置全屏视频回调
     *
     * @param callBack
     */
    public void setFullScreenCallBack(InterAdCallBack callBack) {
        mFullScreenAd.addCallBack(callBack);
    }

    /**
     * 播放激励视频
     */
    public void showRewardedAd(boolean isRewardedVisibility) {
        Logger.d("showRewardedAd");
        if (REWARDED_ERR_TIMES > 3) {
            REWARDED_ERR_TIMES = 0;
            TIME_DELAY_OF_AD += 1000 * 60;
        }
        checkActivity(isRewardedVisibility);
        getInstance().setRewardedVisibility(isRewardedVisibility);
        mRewardAd.loadAd(isRewardedVisibility);
    }

    /**
     * 设置激励视频回调
     *
     * @param callBack
     */
    public void setRewardedCallBack(RewardedCallBack callBack) {
        mRewardAd.addCallBack(callBack);
    }

    /**
     * 开始自动播放
     */
    public void startAutoPlay() {
        mADController.sendEmptyMessageDelayed(TYPE_START, TIME_DELAY_OF_AD);
    }


    /**
     * 如果播放广告可见，需要把游戏activity上层全部关闭，不然影响广告加载
     *
     * @param isAdVisibility
     */
    private void checkActivity(boolean isAdVisibility) {

        if (isAdVisibility) {
//            while (true) {
//                Activity currentActivity = (Activity) ActivityCallbacks.getNewContext();
//                String currentActivityName = currentActivity.getClass().getName();
//                Logger.d("currentActivityName = " + currentActivityName  + "  mGameActivity = " + mGameActivity);
//                if (currentActivityName.equals(mGameActivity.getClass().getName())) {
//                    break;
//                }
//                currentActivity.finish();
//                Logger.d("finish");
//            }
            Activity currentActivity = (Activity) ActivityCallbacks.getNewContext();
            Intent intent = new Intent(currentActivity, mGameActivity.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            currentActivity.startActivity(intent);
        }
    }


    /**
     * 是否进程在前台
     *
     * @param context
     * @return
     */
    public boolean isForeground(Context context) {
        try {
            if (context != null) {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                    if (processInfo.processName.equals(context.getPackageName())) {
                        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }


    /**
     * 关闭媒体音量
     *
     * @param context
     * @return
     */
    private int closeVolume(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        Logger.d("STREAM_SYSTEM = " + am.getStreamVolume(AudioManager.STREAM_SYSTEM));
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        return am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
}