package com.linktower.games;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.linktower.games.callback.BannerCallBack;

import java.util.List;

public class Banner {
    private static TTNativeExpressAd mTTAd;
    private static FrameLayout container;
    private Activity mActivity;
    private BannerCallBack mBannerCallBack;

    public Banner(Activity activity){
        mActivity = activity;
        container = new FrameLayout(activity);
        init(activity);
    }

    public void loadAd() {
        Logger.d("Banner loadAd");
        TTAdNative mTTAdNative = TTAdManagerHolder.get().createAdNative(mActivity);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(Config.getConfig().getBanner_id()) //广告位id
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(500, 45) //期望模板广告view的size,单位dp
                .build();
        //请求广告
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int i, String s) {
                Logger.d("Banner onError " + s);
                if (mBannerCallBack != null){
                    mBannerCallBack.onBannerLoadedFailed();
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null) {
                    return;
                }

                Logger.d("Banner onNativeExpressAdLoad ");
                mTTAd = ads.get(0);
                mTTAd.setSlideIntervalTime(30 * 1000);
                mTTAd.render();
                bindAdListener(mTTAd);
                if (mBannerCallBack != null){
                    mBannerCallBack.onBannerLoaded();
                }
            }

            private void bindAdListener(TTNativeExpressAd ad) {
                ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int i) {

                    }

                    @Override
                    public void onAdShow(View view, int i) {
                        Logger.d("Banner onAdShow ");
                        if (mBannerCallBack != null){
                            mBannerCallBack.onBannerShow();
                        }
                    }

                    @Override
                    public void onRenderFail(View view, String s, int i) {
                        Logger.d("Banner onRenderFail " + s);
                    }

                    @Override
                    public void onRenderSuccess(View view, float v, float v1) {
                        container.removeAllViews();
                        container.addView(view);
                        Logger.d("Banner onRenderSuccess ");
                    }
                });
                bindDislike(ad, false);
            }
        });
    }

    private  void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
            final DislikeInfo dislikeInfo = ad.getDislikeInfo();
            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
                return;
            }
        }
        ad.setDislikeCallback(mActivity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int i, String s, boolean b) {
                //销毁广告
                mTTAd.destroy();
                if (mBannerCallBack != null){
                    mBannerCallBack.onBannerClosed();
                }
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void init(Activity activity) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Utils.dp2px(ActivityCallbacks.getNewContext(), 50));
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layoutParams.bottomMargin = 0;
        activity.addContentView(container, layoutParams);
    }

    public void addCallBack(BannerCallBack callBack) {
        mBannerCallBack = callBack;
    }
}
