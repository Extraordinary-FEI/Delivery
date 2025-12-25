package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    /* ================= 用户信息 ================= */

    public static class UserProfile {
        public int userId;
        public String username;
        public String nickname;
        public String phone;
        public String avatarUrl;

        public UserProfile(int userId, String username, String nickname, String phone, String avatarUrl) {
            this.userId = userId;
            this.username = username;
            this.nickname = nickname;
            this.phone = phone;
            this.avatarUrl = avatarUrl;
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

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        if (columnExists(db, "users", "nickname")) {
            cv.put("nickname", username);
        }
        cv.put("created_at", System.currentTimeMillis());

        long id = db.insert("users", null, cv);
        if (id == -1) {
            return new RegisterResult(false, "注册失败，请重试");
        }

        return new RegisterResult(true, "注册成功");
    }

    private static boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    /* ================= 查询与更新资料 ================= */

    public UserProfile getProfile(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        boolean hasNickname = columnExists(db, "users", "nickname");
        Cursor cursor = db.rawQuery(
                hasNickname
                        ? "SELECT id, username, nickname, phone, avatar_url FROM users WHERE id=? LIMIT 1"
                        : "SELECT id, username, phone, avatar_url FROM users WHERE id=? LIMIT 1",
                new String[]{String.valueOf(userId)}
        );
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            return new UserProfile(
                    cursor.getInt(0),
                    cursor.getString(1),
                    hasNickname ? cursor.getString(2) : cursor.getString(1),
                    hasNickname ? cursor.getString(3) : cursor.getString(2),
                    hasNickname ? cursor.getString(4) : cursor.getString(3)
            );
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean updateProfile(int userId, String nickname, String phone, String avatarUrl) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (columnExists(db, "users", "nickname")) {
            values.put("nickname", nickname);
        }
        values.put("phone", phone);
        values.put("avatar_url", avatarUrl);
        return db.update("users", values, "id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    /* ================= 登录 ================= */

    public LoginResult login(String username, String password, String loginType, String adminCode) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, role FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        try {
            if (c == null || !c.moveToFirst()) {
                return new LoginResult(false, "账号或密码错误", -1, "", "");
            }

            int userId = c.getInt(0);
            String roleInDb = c.getString(1);

            // 普通用户入口：永远只能是 user
            if (!"admin".equals(loginType)) {
                if (!"user".equals(roleInDb)) {
                    return new LoginResult(false, "该账号不是普通用户", -1, "", "");
                }
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
