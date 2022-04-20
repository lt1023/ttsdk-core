package com.linktower.games;

import android.app.Activity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.linktower.games.callback.RewardedCallBack;


public class RewardAd {
    public Activity adActivity;
    private TTRewardVideoAd mttRewardVideoAd;
    private boolean mHasShowDownloadActive = false;
    private RewardedCallBack mRewardedCallBack;

    public RewardAd(Activity activity) {
        adActivity = activity;
    }

    public void loadAd(boolean isReward) {
//        Logger.d("RewardAd loadAd " + isReward);
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot;
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(adActivity);
        adSlot = new AdSlot.Builder()
                .setCodeId(Config.getConfig().getRewarded_id())
                .build();
        //step5:请求广告
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Logger.d("RewardAd onError " + message);
                LinkSDK.getInstance().onAdError(LinkSDK.TYPE.TYPE_REWARDED);
                if (mRewardedCallBack != null) {
                    mRewardedCallBack.onRewardedLoadedFailed();
                }
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
//                Log.w("进入进入: ","555555");
//                showAD();
            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ttRewardVideoAd) {
                Logger.d("RewardAd onRewardVideoCached ");
                showAD();
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                if (mRewardedCallBack != null) {
                    mRewardedCallBack.onRewardedLoaded();
                }
                mttRewardVideoAd = ad;
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        Logger.d("RewardAd onAdShow ");
                        if (isReward) {
                            if (mRewardedCallBack != null) {
                                mRewardedCallBack.onRewardedShow();
                            }
                        }
                    }

                    @Override
                    public void onAdVideoBarClick() {

                    }

                    @Override
                    public void onAdClose() {
                        Logger.d("RewardAd onAdClose ");
                        if (isReward) {
                            if (mRewardedCallBack != null) {
                                mRewardedCallBack.onRewardedClosed();
                            }
                        }
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        Logger.d("RewardAd onVideoComplete ");

                    }

                    @Override
                    public void onVideoError() {
                        Logger.d("RewardAd onVideoError ");
                        if (isReward) {
                            if (mRewardedCallBack != null) {
                                mRewardedCallBack.onRewardedClosed();
                            }
                        }
                    }

                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
                        if (isReward) {
                            if (rewardVerify) {
                                if (mRewardedCallBack != null) {
                                    mRewardedCallBack.onRewardedEarned();
                                }
                            }
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
//                        Log.v(TAG, "Callback --> rewardVideoAd has onSkippedVideo");
                    }
                });

                mttRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
//                        Log.v("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            ;
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                        Log.v("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                        Log.v("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                        Log.v("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
//                        Log.v("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
                    }
                });
            }
        });
    }

    public void showAD() {
        Logger.d("RewardAd showAD ");
        if (mttRewardVideoAd != null) {
            mttRewardVideoAd.showRewardVideoAd(adActivity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
            mttRewardVideoAd = null;
        } else {
            Logger.d("RewardAd 清先加载广告 ");
        }
    }

    public void addCallBack(RewardedCallBack callBack) {
        mRewardedCallBack = callBack;
    }
}
