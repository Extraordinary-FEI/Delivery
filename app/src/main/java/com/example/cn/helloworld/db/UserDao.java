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
        public int points;
        public String birthday;

        public UserProfile(int userId, String username, String nickname, String phone, String avatarUrl, int points,
                           String birthday) {
            this.userId = userId;
            this.username = username;
            this.nickname = nickname;
            this.phone = phone;
            this.avatarUrl = avatarUrl;
            this.points = points;
            this.birthday = birthday;
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
        Cursor cursor = db.query("users", null, "id=?", new String[]{String.valueOf(userId)},
                null, null, null, "1");
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            int idIndex = cursor.getColumnIndex("id");
            int usernameIndex = cursor.getColumnIndex("username");
            int nicknameIndex = cursor.getColumnIndex("nickname");
            int phoneIndex = cursor.getColumnIndex("phone");
            int avatarIndex = cursor.getColumnIndex("avatar_url");
            int pointsIndex = cursor.getColumnIndex("points");
            int birthdayIndex = cursor.getColumnIndex("birthday");
            int points = pointsIndex >= 0 ? cursor.getInt(pointsIndex) : 0;
            String username = usernameIndex >= 0 ? cursor.getString(usernameIndex) : "";
            String nickname = nicknameIndex >= 0 ? cursor.getString(nicknameIndex) : username;
            String phone = phoneIndex >= 0 ? cursor.getString(phoneIndex) : "";
            String avatarUrl = avatarIndex >= 0 ? cursor.getString(avatarIndex) : null;
            String birthday = birthdayIndex >= 0 ? cursor.getString(birthdayIndex) : null;
            return new UserProfile(
                    idIndex >= 0 ? cursor.getInt(idIndex) : userId,
                    username,
                    nickname,
                    phone,
                    avatarUrl,
                    points,
                    birthday
            );
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getPoints(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        if (!columnExists(db, "users", "points")) {
            return 0;
        }
        Cursor cursor = db.rawQuery("SELECT points FROM users WHERE id=? LIMIT 1",
                new String[]{String.valueOf(userId)});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    public boolean addPoints(int userId, int delta) {
        if (delta <= 0) {
            return false;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        if (!columnExists(db, "users", "points")) {
            return false;
        }
        db.execSQL("UPDATE users SET points = points + ? WHERE id = ?",
                new Object[]{delta, userId});
        return true;
    }

    public boolean deductPoints(int userId, int delta) {
        if (delta <= 0) {
            return false;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        if (!columnExists(db, "users", "points")) {
            return false;
        }
        db.execSQL("UPDATE users SET points = CASE WHEN points >= ? THEN points - ? ELSE points END WHERE id = ?",
                new Object[]{delta, delta, userId});
        return true;
    }

    public boolean updateProfile(int userId, String nickname, String phone, String avatarUrl) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (columnExists(db, "users", "nickname")) {
            values.put("nickname", nickname);
        }
        values.put("phone", phone);
        if (columnExists(db, "users", "avatar_url")) {
            values.put("avatar_url", avatarUrl);
        }
        return db.update("users", values, "id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public String getBirthday(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        if (!columnExists(db, "users", "birthday")) {
            return null;
        }
        Cursor cursor = db.rawQuery("SELECT birthday FROM users WHERE id=? LIMIT 1",
                new String[]{String.valueOf(userId)});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public boolean updateBirthday(int userId, String birthday) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (!columnExists(db, "users", "birthday")) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put("birthday", birthday);
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
