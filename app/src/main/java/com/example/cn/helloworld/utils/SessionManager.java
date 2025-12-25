package com.example.cn.helloworld.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_ROLE = "current_role";
    private static final String KEY_TOKEN = "current_token";
    private static final String KEY_USER_ID = "current_user_id";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    private SessionManager() {
    }

    public static void saveSession(Context context, String username, String role, String token, String userId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_CURRENT_USER, username)
                .putString(KEY_ROLE, role)
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
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

    public static String getToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_TOKEN, "");
    }

    public static String getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USER_ID, "");
    }

    public static void clearSession(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    public static boolean isAdmin(Context context) {
        return ROLE_ADMIN.equals(getRole(context));
    }
}
