package com.linktower.games.callback;

/**
 * 激励视频广告open回调
 */
public interface RewardedCallBack {
    /**
     * 激励视频加载成功
     */
    void onRewardedLoaded();

    /**
     * 激励视频加载失败
     */
    void onRewardedLoadedFailed();

    /**
     * 激励视频播放回调
     */
    void onRewardedShow();

    /**
     * 获得奖励回调
     */
    void onRewardedEarned();

    /**
     * 视频关闭
     */
    void onRewardedClosed();
}