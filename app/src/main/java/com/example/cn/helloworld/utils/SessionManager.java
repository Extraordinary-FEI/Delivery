package com.example.cn.helloworld.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_ROLE = "current_role";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    private SessionManager() {
    }

    public static void saveSession(Context context, String username, String role) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_CURRENT_USER, username)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public static String getCurrentUser(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_CURRENT_USER, "");
    }

    public static String getRole(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_ROLE, ROLE_USER);
    }

    public static boolean isAdmin(Context context) {
        return ROLE_ADMIN.equals(getRole(context));
    }
}
