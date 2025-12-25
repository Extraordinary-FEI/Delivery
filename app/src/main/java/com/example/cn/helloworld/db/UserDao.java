package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class UserDao {

    // 固定管理员验证码（演示用）
    public static final String ADMIN_CODE = "8888";

    private DBHelper helper;

    public UserDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public static class LoginResult {
        public boolean ok;
        public String msg;
        public int userId;
        public String role;
        public String username;

        public LoginResult(boolean ok, String msg, int userId, String role, String username) {
            this.ok = ok;
            this.msg = msg;
            this.userId = userId;
            this.role = role;
            this.username = username;
        }
    }

    /**
     * 注册：registerType = "user" 或 "admin"
     * admin 注册必须 adminCode = 8888
     */
    public boolean register(String username, String password, String registerType, String adminCode) {
        if (username == null || username.trim().length() == 0) return false;
        if (password == null || password.trim().length() == 0) return false;

        String role = "user";

        if ("admin".equals(registerType)) {
            if (!ADMIN_CODE.equals(adminCode)) return false;
            role = "admin";
        }

        SQLiteDatabase db = helper.getWritableDatabase();

        // 查重
        Cursor c = db.rawQuery("SELECT id FROM users WHERE username=?", new String[]{username});
        try {
            if (c != null && c.moveToFirst()) {
                return false; // 用户名已存在
            }
        } finally {
            if (c != null) c.close();
        }

        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password); // 简化：明文（需要更安全我再给你加 hash）
        cv.put("role", role);
        cv.put("created_at", System.currentTimeMillis());

        long id = db.insert("users", null, cv);
        return id != -1;
    }

    /**
     * 登录：loginType = "user" 或 "admin"
     * admin 登录必须 adminCode = 8888
     *
     * 关键：普通登录不允许返回 admin 身份（即使账号是 admin）
     */
    public LoginResult login(String username, String password, String loginType, String adminCode) {

        if (username == null || username.trim().length() == 0) {
            return new LoginResult(false, "用户名不能为空", -1, "", "");
        }
        if (password == null || password.trim().length() == 0) {
            return new LoginResult(false, "密码不能为空", -1, "", "");
        }

        boolean wantsAdmin = "admin".equals(loginType);

        if (wantsAdmin) {
            if (!ADMIN_CODE.equals(adminCode)) {
                return new LoginResult(false, "管理员验证码错误", -1, "", "");
            }
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, role FROM users WHERE username=? AND password=?",
                new String[]{username, password});

        try {
            if (c == null || !c.moveToFirst()) {
                return new LoginResult(false, "账号或密码错误", -1, "", "");
            }

            int userId = c.getInt(0);
            String roleInDb = c.getString(1);

            // ✅ 普通入口：永远返回 user
            if (!wantsAdmin) {
                return new LoginResult(true, "登录成功", userId, "user", username);
            }

            // ✅ 管理员入口：必须数据库里也是 admin
            if (!"admin".equals(roleInDb)) {
                return new LoginResult(false, "该账号不是管理员", -1, "", "");
            }

            return new LoginResult(true, "登录成功", userId, "admin", username);

        } finally {
            if (c != null) c.close();
        }
    }
}
