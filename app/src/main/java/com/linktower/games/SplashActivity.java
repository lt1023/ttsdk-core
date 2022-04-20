package com.linktower.games;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.linktower.R;
import com.linktower.application.GameActivity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SplashActivity extends Activity {
    private TTAdNative mTTAdNative;
    private FrameLayout mSplashContainer;
    //是否强制跳转到主页面
    private boolean mForceGoMain;
    //开屏广告加载超时时间,建议大于3000,这里为了冷启动第一次加载到广告并且展示,示例设置了3000ms
    private static final int AD_TIME_OUT = 3000;
    private boolean mIsExpress = false; //是否请求模板广告
    private boolean mIsHalfSize = false;//是否是半全屏开屏
    private boolean mIsSplashClickEye = false;//是否是开屏点睛
    private LinearLayout mSplashHalfSizeLayout;
    private FrameLayout mSplashSplashContainer;
    private TTSplashAd mSplashAd;
    private SharedPreferences config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_splash);
        LinkSDK.getInstance().checkNetWork(this);
        mSplashContainer = (FrameLayout) findViewById(R.id.splash_container);
        mSplashHalfSizeLayout = (LinearLayout) findViewById(R.id.splash_half_size_layout);
        mSplashSplashContainer = (FrameLayout) findViewById(R.id.splash_container_half_size);
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        config = getSharedPreferences("game_config", MODE_PRIVATE);
        boolean isAgree = config.getBoolean("is_agree", false);
        if (!isAgree) {
            View inflate = LayoutInflater.from(this).inflate(R.layout.activity_privacy, null);
            TextView privacy_title = inflate.findViewById(R.id.privacy_title);
            String res_title = getResources().getString(R.string.privacy_title);
            String appName = getAppName(this);
            String title = String.format(res_title, appName);
            privacy_title.setText(title);
            inflate.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSplashContainer.removeAllViews();
                    requestPermissions();
                }
            });
            inflate.findViewById(R.id.privacy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(SplashActivity.this, Privacy.class));
                }
            });

            inflate.findViewById(R.id.protocol).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(SplashActivity.this, Protocol.class));
                }
            });
            mSplashContainer.addView(inflate);
        } else {
            requestPermissions();
        }
    }

    private String getAppName(Context context) {
        if (context != null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                return String.valueOf(packageManager.getApplicationLabel(context.getApplicationInfo()));
            } catch (Exception ignore) {
            }
        }
        return "";
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            config.edit().putBoolean("is_agree", true).apply();
            loadSplashAd();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i : grantResults) {
                if (i != PERMISSION_GRANTED) {
//                    Toast.makeText(SplashActivity.this, "为了正常运行游戏，请您务必开启权限！", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("重要提示");
                    String message = "应用缺少SDK运行必须的权限,感谢您的支持!";
                    builder.setMessage(message);
                    builder.setNegativeButton("授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions();
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return;
                }
            }
            config.edit().putBoolean("is_agree", true).apply();
            loadSplashAd();
        }
    }

    private void loadSplashAd() {
        SplashClickEyeManager.getInstance().setSupportSplashClickEye(false);
        //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        float splashWidthDp = UIUtils.getScreenWidthDp(this);
        int splashWidthPx = UIUtils.getScreenWidthInPx(this);
        int screenHeightPx = UIUtils.getScreenHeight(this);
        float screenHeightDp = UIUtils.px2dip(this, screenHeightPx);
        float splashHeightDp;
        int splashHeightPx;


        if (mIsHalfSize) {
            // 开屏高度 = 屏幕高度 - 下方预留的高度，demo中是预留了屏幕高度的1/5，因此开屏高度传入 屏幕高度*4/5
            splashHeightDp = screenHeightDp * 4 / 5.f;
            splashHeightPx = (int) (screenHeightPx * 4 / 5.f);
        } else {
            splashHeightDp = screenHeightDp;
            splashHeightPx = screenHeightPx;
        }

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(Config.getConfig().getSplash_id())
                //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
                //view宽高等于图片的宽高
                .setExpressViewAcceptedSize(splashWidthDp, splashHeightDp) // 单位是dp
                .setImageAcceptedSize(splashWidthPx, splashHeightPx) // 单位是px
                .build();

        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            public void onError(int code, String message) {
                Logger.d("Splash onError " + code + "  : " + message);
                delay();
            }

//            AdSlot adSlot = new AdSlot.Builder()
//                    .setCodeId(mCodeId)
//                    //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
//                    //view宽高等于图片的宽高
//                    .setExpressViewAcceptedSize(splashWidthDp, splashHeightDp) // 单位是dp
//                    .setImageAcceptedSize(splashWidthPx, splashHeightPx) // 单位是px
//                    .build();

            @Override
            public void onTimeout() {
                Logger.d("Splash onTimeout ");
                delay();
            }

            @Override
            public void onSplashAdLoad(TTSplashAd ad) {
                Logger.d("Splash onSplashAdLoad ");

                if (ad == null) {
                    return;
                }
                mSplashAd = ad;
                //获取SplashView
                View view = ad.getSplashView();
                if (mIsHalfSize) {
                    if (view != null && mSplashSplashContainer != null && !SplashActivity.this.isFinishing()) {
                        mSplashHalfSizeLayout.setVisibility(View.VISIBLE);
                        mSplashSplashContainer.setVisibility(View.VISIBLE);
                        if (mSplashContainer != null) {
                            mSplashContainer.setVisibility(View.GONE);
                        }
                        mSplashSplashContainer.removeAllViews();
                        //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕高
                        mSplashSplashContainer.addView(view);
                    } else {
                        delay();
                    }
                } else {
                    if (view != null && mSplashContainer != null && !SplashActivity.this.isFinishing()) {
                        mSplashContainer.setVisibility(View.VISIBLE);
                        if (mSplashHalfSizeLayout != null) {
                            mSplashHalfSizeLayout.setVisibility(View.GONE);
                        }

                        mSplashContainer.removeAllViews();
                        //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕高
                        mSplashContainer.addView(view);
                    } else {
                        delay();
                    }
                }

                //设置SplashView的交互监听器
                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
//                        Log.w("进入进入","开屏广告点击");
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        Logger.d("Splash onAdShow ");
                    }

                    @Override
                    public void onAdSkip() {
//                        Log.w("进入进入","开屏广告跳过");
                        delay();
                    }

                    @Override
                    public void onAdTimeOver() {
//                        Log.w("进入进入","开屏广告倒计时结束");
                        delay();
                    }
                });
            }
        }, AD_TIME_OUT);
    }

    private void delay() {
        next();
    }

    private void next() {
        try {
            Intent intent = null;
            String metaActivity = getActivity();
            if (TextUtils.isEmpty(metaActivity)){
                intent = new Intent(SplashActivity.this, GameActivity.class);
            }else {
                Class<?> forName = Class.forName(metaActivity);
                intent = new Intent(SplashActivity.this, forName);
            }
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getActivity() {
        ApplicationInfo info = null;
        try {
            info = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert info != null;
        return info.metaData.getString("game_activity");
    }
}