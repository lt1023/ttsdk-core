package com.linktower.games.callback;

public interface BannerCallBack {
    /**
     * Banner加载成功
     */
    void onBannerLoaded();

    /**
     * Banner加载失败
     */
    void onBannerLoadedFailed();

    /**
     * Banner播放回调
     */
    void onBannerShow();

    /**
     * Banner关闭
     */
    void onBannerClosed();
}
