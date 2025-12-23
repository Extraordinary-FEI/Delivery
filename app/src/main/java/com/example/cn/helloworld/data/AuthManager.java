package com.example.cn.helloworld.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREFS = "auth_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    public static void register(Context context, String username, String password) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public static boolean validate(Context context, String username, String password) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String savedUser = preferences.getString(KEY_USERNAME, null);
        String savedPass = preferences.getString(KEY_PASSWORD, null);
        return username != null && password != null
                && username.equals(savedUser)
                && password.equals(savedPass);
    }
}
