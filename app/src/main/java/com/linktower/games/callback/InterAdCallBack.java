package com.linktower.games.callback;

/**
 * 插屏广告和全屏视频广告open回调
 */
public interface InterAdCallBack {
    /**
     * 插屏广告和全屏视频加载成功
     */
    void onInterAdLoaded();

    /**
     * 插屏广告和全屏视频加载失败
     */
    void onInterAdLoadedFailed();

    /**
     * 插屏广告和全屏视频播放回调
     */
    void onInterAdShow();

    /**
     * 插屏广告和全屏视频关闭
     */
    void onInterAdClosed();
}
