package com.linktower.games;

import android.content.Context;
import android.content.SharedPreferences;

public class SDKSharedPreferences {
    private static SDKSharedPreferences instance;
    private final String SHAREPREFS_NAME = "linksdk";

    public static SDKSharedPreferences getInstance() {
        if (instance == null) {
            instance = new SDKSharedPreferences();
        }
        return instance;
    }

    private SharedPreferences mSharedPreferences;

    public SDKSharedPreferences init(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHAREPREFS_NAME, Context.MODE_PRIVATE);
        return this;
    }

    public int getTodayAdTimes(String date) {
        return mSharedPreferences.getInt(date, 0);
    }

    public void addTodayAdTimes(String date, int times) {
        int todayAdTimes = getTodayAdTimes(date);
        todayAdTimes += times;
        mSharedPreferences.edit().putInt(date, todayAdTimes).apply();
    }
}
