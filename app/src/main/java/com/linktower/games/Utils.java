package com.linktower.games;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public final class Utils {

    public static void changeScreenOrientation(Activity activity) {
        if (Utils.getOrientation(activity) == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static int getOrientation(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.orientation;
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    public static float px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxValue / (scale <= 0 ? 1 : scale) + 0.5f);
    }

    public static float getScreenWidthDP(Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        float width = context.getResources().getDisplayMetrics().widthPixels;
        return width / (scale <= 0 ? 1 : scale) + 0.5f;
    }

}

