package com.linktower.games;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Report {
    private static Report instance;

    public static Report getInstance() {
        if (instance == null){
            instance = new Report();
        }
        return instance;
    }


    /**
     * 上报233买量链接
     */
    public void report(Context context) {
        try {
            HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("appId", "10277");
            paramsMap.put("deviceId", getDeviceId(context));
            paramsMap.put("imei", getImei(ActivityCallbacks.getNewContext()));
            paramsMap.put("oaid", getOaid(ActivityCallbacks.getNewContext()));
            paramsMap.put("idfa", "");
            paramsMap.put("timestamp", System.currentTimeMillis() + "");
            paramsMap.put("nonce", (100 + new Random().nextInt(9000)) + "");
            paramsMap.put("sign", getSign(paramsMap));
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                //追加表单信息
                String value = paramsMap.get(key);
                if (value == null) {
                    value = "";
                }
                builder.add(key, value);
            }
            Request request = new Request.Builder().url("https://click.tangro.cn/v3/app/launch").post(builder.build()).build();
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                SXLog.d("onFailure" + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
//                SXLog.d(response.body().string());
//                    Logger.d("" + response.body().string());
                }
            });
        } catch (Exception ignore) {
        }
    }

    private String getDeviceId(Context context) {
        try {
            String androidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return androidId;
        } catch (Exception e) {
        }
        return "";
    }

    private String getImei(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = tm.getDeviceId();
            return deviceId;
        } catch (Exception e) {
        }
        return "";
    }

    private String getOaid(Context context) {
        try {
            return Build.SERIAL;
        } catch (Exception ignore) {
        }

        return "";
    }

    private static class MapKeyComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {

            return str1.compareTo(str2);
        }
    }

    private Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, String> sortMap = new TreeMap<String, String>(
                new MapKeyComparator()
        );

        sortMap.putAll(map);

        return sortMap;
    }

    public  String getSha1(String str) {

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));
            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return "";
        }
    }

    private String getSign(Map<String, String> map) {
        String appSecret = "8899513dd177037d";
        Map<String, String> resultMap = sortMapByKey(map);
        StringBuilder builder = new StringBuilder();
        for (String key : resultMap.keySet()) {
            builder.append(resultMap.get(key));
        }
        builder.append(appSecret);
        String string = builder.toString();
        return getSha1(string);
    }
}
