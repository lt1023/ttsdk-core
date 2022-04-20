package com.linktower.games;

import android.app.Activity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.linktower.games.callback.InterAdCallBack;

public class FullScreenAd implements TTFullScreenVideoAd.FullScreenVideoAdInteractionListener , TTAdNative.FullScreenVideoAdListener {
    private TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullVideoAd;
    private boolean mIsLoaded = false; //视频是否加载完成
    private String mFullVideoId;
    private Activity mActivity;
    private boolean mHasShowDownloadActive = false;
    private InterAdCallBack mFullScreenAdCallBack;
    private AdSlot adSlot;

    FullScreenAd(Activity activity){
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        mTTAdNative = ttAdManager.createAdNative(activity);
        mFullVideoId = Config.getConfig().getFullscreenad_id();
        mActivity = activity;
        adSlot = new AdSlot.Builder()
                .setCodeId(mFullVideoId)
                .setExpressViewAcceptedSize(500, 500)
                .setSupportDeepLink(true)
                .setOrientation(TTAdConstant.HORIZONTAL)
                .build();
    }

    public void loadAd(){
        mTTAdNative.loadFullScreenVideoAd(adSlot, this);
    }

    @Override
    public void onAdShow() {
        Logger.d("FullScreenVideoAd onAdShow");
        if (mFullScreenAdCallBack != null){
            mFullScreenAdCallBack.onInterAdShow();
        }
    }

    @Override
    public void onAdVideoBarClick() {

    }

    @Override
    public void onAdClose() {
        Logger.d("FullScreenVideoAd onAdClose");
        if (mFullScreenAdCallBack != null){
            mFullScreenAdCallBack.onInterAdClosed();
        }
    }

    @Override
    public void onVideoComplete() {

    }

    @Override
    public void onSkippedVideo() {

    }

    @Override
    public void onError(int i, String s) {
        Logger.d("FullScreenVideoAd onError " + s);
        LinkSDK.getInstance().onAdError(LinkSDK.TYPE.TYPE_FULLSCREEN);
        if (mFullScreenAdCallBack != null){
            mFullScreenAdCallBack.onInterAdLoadedFailed();
        }
    }

    @Override
    public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {

    }

    @Override
    public void onFullScreenVideoCached() {

    }

    @Override
    public void onFullScreenVideoCached(TTFullScreenVideoAd ad) {
        if (mFullScreenAdCallBack != null){
            mFullScreenAdCallBack.onInterAdLoaded();
        }
        mttFullVideoAd = ad;
        mIsLoaded = false;
        mttFullVideoAd.setFullScreenVideoAdInteractionListener(this);
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                mHasShowDownloadActive = false;
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
//                    Logger.d("下载中，点击下载区域暂停");
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                Logger.d("下载暂停，点击下载区域继续");
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                Logger.d("下载失败，点击下载区域重新下载");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                Logger.d("下载完成，点击下载区域重新下载");
            }

            @Override
            public void onInstalled(String fileName, String appName) {
//                Logger.d("安装完成，点击下载区域打开");
            }
        });
        mttFullVideoAd.showFullScreenVideoAd(mActivity);
    }

    public void addCallBack(InterAdCallBack callBack) {
        mFullScreenAdCallBack = callBack;
    }
}
