package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.security.MessageDigest;

public class UserDao {

    public static final String ADMIN_CODE = "8888";

    private DBHelper helper;

    public UserDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    /* ================= 注册结果 ================= */

    public static class RegisterResult {
        public boolean ok;
        public String msg;

        public RegisterResult(boolean ok, String msg) {
            this.ok = ok;
            this.msg = msg;
        }
    }

    /* ================= 登录结果 ================= */

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

    /* ================= 工具：SHA-256 ================= */

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /* ================= 用户名查重 ================= */

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM users WHERE username=? LIMIT 1",
                new String[]{username}
        );
        try {
            return c != null && c.moveToFirst();
        } finally {
            if (c != null) c.close();
        }
    }

    /* ================= 注册 ================= */

    public RegisterResult register(String username, String password, String role, String adminCode) {

        if (isUsernameTaken(username)) {
            return new RegisterResult(false, "用户名已存在");
        }

        if ("admin".equals(role)) {
            if (!ADMIN_CODE.equals(adminCode)) {
                return new RegisterResult(false, "管理员验证码错误");
            }
        } else {
            role = "user"; // 强制兜底
        }

        String passwordHash = sha256(password);
        if (passwordHash.length() == 0) {
            return new RegisterResult(false, "密码处理失败");
        }

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password_hash", passwordHash);
        cv.put("role", role);
        cv.put("created_at", System.currentTimeMillis());

        long id = db.insert("users", null, cv);
        if (id == -1) {
            return new RegisterResult(false, "注册失败，请重试");
        }

        return new RegisterResult(true, "注册成功");
    }

    /* ================= 登录 ================= */

    public LoginResult login(String username, String password, String loginType, String adminCode) {

        String passwordHash = sha256(password);

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, role FROM users WHERE username=? AND password_hash=?",
                new String[]{username, passwordHash}
        );

        try {
            if (c == null || !c.moveToFirst()) {
                return new LoginResult(false, "账号或密码错误", -1, "", "");
            }

            int userId = c.getInt(0);
            String roleInDb = c.getString(1);

            // 普通用户入口：永远只能是 user
            if (!"admin".equals(loginType)) {
                return new LoginResult(true, "登录成功", userId, "user", username);
            }

            // 管理员入口：必须验证码 + 数据库角色为 admin
            if (!ADMIN_CODE.equals(adminCode)) {
                return new LoginResult(false, "管理员验证码错误", -1, "", "");
            }

            if (!"admin".equals(roleInDb)) {
                return new LoginResult(false, "该账号不是管理员", -1, "", "");
            }

            return new LoginResult(true, "登录成功", userId, "admin", username);

        } finally {
            if (c != null) c.close();
        }
    }
}
