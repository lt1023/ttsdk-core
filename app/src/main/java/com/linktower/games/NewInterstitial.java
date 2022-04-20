package com.linktower.games;

import android.app.Activity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.linktower.games.callback.InterAdCallBack;

public class NewInterstitial {
    private TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullVideoAd;
    private Activity mContext;
    private boolean mIsLoaded = false; //视频是否加载完成
    private boolean isADShowing = false;
    private InterAdCallBack mInterAdCallBack;

    public NewInterstitial(Activity activity){
        this.mContext = activity;
    }

    public void loadAd() {
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(mContext);
        AdSlot adSlot;
        adSlot = new AdSlot.Builder()
                .setCodeId(Config.getConfig().getInterad_id())
                //模板广告需要设置期望个性化模板广告的大小,单位dp,全屏视频场景，只要设置的值大于0即可
                .setExpressViewAcceptedSize(500,500)
                .build();

        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int i, String s) {
               Logger.d("NewInterstitial onError " + s);
               LinkSDK.getInstance().onAdError(LinkSDK.TYPE.TYPE_INTERAD);
               if (mInterAdCallBack != null){
                   mInterAdCallBack.onInterAdLoadedFailed();
               }
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                if (mInterAdCallBack != null){
                    mInterAdCallBack.onInterAdLoaded();
                }
                mttFullVideoAd = ad;
                mIsLoaded = false;
                mttFullVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        Logger.d("NewInterstitial onAdShow");
                        if (mInterAdCallBack != null){
                            mInterAdCallBack.onInterAdShow();
                        }
                        if (mttFullVideoAd != null) {
                            isADShowing = true;
                        }
                    }

                    @Override
                    public void onAdVideoBarClick() {
//                        Log.w("进入进入","onFullScreenVideoAd onAdVideoBarClick");
                    }

                    @Override
                    public void onAdClose() {
                        Logger.d("NewInterstitial onAdClose");
                        isADShowing = false;
                        if (mInterAdCallBack != null){
                            mInterAdCallBack.onInterAdClosed();
                        }
                    }

                    @Override
                    public void onVideoComplete() {
//                        Log.w("进入进入","onVideoComplete");
                    }

                    @Override
                    public void onSkippedVideo() {
//                        Log.w("进入进入","onSkippedVideo");
                    }
                });
            }

            @Override
            public void onFullScreenVideoCached() {
//                mIsLoaded = true;
//                mttFullVideoAd.showFullScreenVideoAd((Activity) mContext, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
            }

            @Override
            public void onFullScreenVideoCached(TTFullScreenVideoAd ttFullScreenVideoAd) {
                mIsLoaded = true;
                mttFullVideoAd.showFullScreenVideoAd(mContext, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
            }
        });
    }

    public void addCallBack(InterAdCallBack callBack) {
        mInterAdCallBack = callBack;
    }
}
