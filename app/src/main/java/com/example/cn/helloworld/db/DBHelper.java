package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "delivery.db";
    public static final int DB_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL UNIQUE," +
                        "password_hash TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "created_at INTEGER NOT NULL" +
                        ");"
        );

        // 可选：初始化一个默认管理员
        db.execSQL("INSERT OR IGNORE INTO users(username,password_hash,role,created_at) " +
                "VALUES('admin','" + sha256("123456") + "','admin'," + System.currentTimeMillis() + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN password_hash TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN created_at INTEGER");

            Cursor cursor = db.rawQuery("SELECT id, password FROM users", null);
            try {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String password = cursor.getString(1);
                    ContentValues values = new ContentValues();
                    values.put("password_hash", sha256(password == null ? "" : password));
                    values.put("created_at", System.currentTimeMillis());
                    db.update("users", values, "id=?", new String[]{String.valueOf(id)});
                }
            } finally {
                cursor.close();
            }
        }
    }

    private static String sha256(String input) {
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
}
