package com.linktower.games;

import android.util.Log;

public class Logger {
    private static boolean isDebug = true;//发布前改为true
    private static String TAG = "linksdk";

    public static void d(String str) {
        if (isDebug) {
            Log.d(TAG, str);
        }
    }

    public static void e(String str) {
        if (isDebug) {
            Log.e(TAG, str);
        }
    }

    public static void e(String str, Throwable throwable) {
        if (isDebug) {
            Log.e(TAG, str, throwable);
        }
    }

    public static void setDebug(boolean debug){
        isDebug = debug;
    }

}
