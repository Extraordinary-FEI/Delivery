package com.example.cn.helloworld.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public final class LocaleManager {
    private static final String PREFS_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "language_code";
    private static final String DEFAULT_LANGUAGE = "zh-CN";

    private LocaleManager() {
    }

    public static Context applyLocale(Context context) {
        return updateResources(context, getSavedLanguage(context));
    }

    public static void setNewLocale(Context context, String languageTag) {
        saveLanguage(context, languageTag);
        updateResources(context, languageTag);
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    private static void saveLanguage(Context context, String languageTag) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_LANGUAGE, languageTag).apply();
    }

    private static Context updateResources(Context context, String languageTag) {
        Locale locale = buildLocale(languageTag);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        }

        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        return context;
    }

    private static Locale buildLocale(String languageTag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Locale.forLanguageTag(languageTag);
        }

        String[] parts = languageTag.split("-");
        if (parts.length >= 2) {
            return new Locale(parts[0], parts[1]);
        }

        return new Locale(languageTag);
    }
}

