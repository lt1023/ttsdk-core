package com.linktower.application;

import android.content.Context;

public class GameSDK {
    /**
     * SDK初始化
     * @param application
     */
    public static native void onCreate(android.app.Application application);

    //
    public static native void init(Context base);


    /**
     *
     * @return 配置表文件名
     */
    public static native String getName();

}
