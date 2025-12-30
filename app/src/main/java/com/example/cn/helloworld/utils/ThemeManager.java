package com.example.cn.helloworld.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatDelegate;

public final class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode";

    private ThemeManager() {
    }

    public static void apply(Context context) {
        AppCompatDelegate.setDefaultNightMode(isNightMode(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean isNightMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_NIGHT_MODE, false);
    }

    public static void setNightMode(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_NIGHT_MODE, enabled).apply();
        apply(context);
    }
}
