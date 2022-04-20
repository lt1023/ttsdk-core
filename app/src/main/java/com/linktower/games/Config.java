package com.linktower.games;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.linktower.application.GameSDK;
import com.linktower.games.aes.ScriptUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Config {

    private static Bean mData;

    public static Bean getConfig(){
//        generateConfig();
        return mData;
    }

    private static String getData(AssetManager manager, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    manager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            Logger.e("" , e);
        }
        return "";
    }

    /**
     * ！！！！生成参数后将此方法注释
     * 生成加密参数，将result复制到Assets下tt_83406806ff99500e8e9cffc9875c2a文件
     * @return
     */
//    private static String generateConfig(){
//        Bean bean = new Bean();
//        bean.setApp_id("5277318");
//        bean.setBanner_id("948062429");
//        bean.setFullscreenad_id("948113048");
//        bean.setInterad_id("948062427");
//        bean.setRewarded_id("948062428");
//        bean.setNative_id("");
//        bean.setSplash_id("887715252");
//        bean.setUmeng_id("6226c2572b8de26e11e94809");
//        String json = new Gson().toJson(bean);
////        Logger.d("json = " + json);
////        Logger.d("key = " +  ScriptUtil.decode(GameSDK.getName()));
//        String encrypt = ScriptUtil.encrypt(ScriptUtil.decode(GameSDK.getName()), json);
////        Logger.d("encrypt = " + encrypt);
//        String result = ScriptUtil.encode(encrypt).replaceAll("\\n", "");
//        Logger.d("result = " + result);//加密最终结果
//        return result;
////        return null;
//    }

    public static void init(Context context){
        AssetManager assetManager = context.getAssets();
        String data = getData(assetManager, ScriptUtil.decode(GameSDK.getName()));
        praseData(data);
    }

    private static void praseData(String data) {
        String decode = ScriptUtil.decrypt(ScriptUtil.decode(GameSDK.getName()), ScriptUtil.decode(data));
        mData = new Gson().fromJson(decode, Bean.class);
        Logger.d(decode);
    }


    public static class Bean{
        private String app_id;
        private String splash_id;
        private String banner_id;
        private String interad_id;
        private String fullscreenad_id;
        private String rewarded_id;
        private String native_id;
        private String umeng_id;

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public String getSplash_id() {
            return splash_id;
        }

        public void setSplash_id(String splash_id) {
            this.splash_id = splash_id;
        }

        public String getBanner_id() {
            return banner_id;
        }

        public void setBanner_id(String banner_id) {
            this.banner_id = banner_id;
        }

        public String getInterad_id() {
            return interad_id;
        }

        public void setInterad_id(String interad_id) {
            this.interad_id = interad_id;
        }

        public String getFullscreenad_id() {
            return fullscreenad_id;
        }

        public void setFullscreenad_id(String fullscreenad_id) {
            this.fullscreenad_id = fullscreenad_id;
        }

        public String getRewarded_id() {
            return rewarded_id;
        }

        public void setRewarded_id(String rewarded_id) {
            this.rewarded_id = rewarded_id;
        }

        public String getNative_id() {
            return native_id;
        }

        public void setNative_id(String native_id) {
            this.native_id = native_id;
        }

        public String getUmeng_id() {
            return umeng_id;
        }

        public void setUmeng_id(String umeng_id) {
            this.umeng_id = umeng_id;
        }
    }
}