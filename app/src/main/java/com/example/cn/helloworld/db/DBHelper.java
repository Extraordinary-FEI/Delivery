package com.example.cn.helloworld.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "delivery.db";
    public static final int DB_VERSION = 4;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL UNIQUE," +
                        "password TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "phone TEXT," +
                        "nickname TEXT," +
                        "avatar_url TEXT," +
                        "created_at INTEGER NOT NULL" +
                        ");"
        );

        // 可选：初始化一个默认管理员
        db.execSQL("INSERT OR IGNORE INTO users(username,password,role,created_at) " +
                "VALUES('admin','123456','admin'," + System.currentTimeMillis() + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            if (!columnExists(db, "users", "created_at")) {
                db.execSQL("ALTER TABLE users ADD COLUMN created_at INTEGER");
            }
        }

        if (oldVersion < 3) {
            if (!columnExists(db, "users", "phone")) {
                db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
            }
            if (!columnExists(db, "users", "nickname")) {
                db.execSQL("ALTER TABLE users ADD COLUMN nickname TEXT");
            }
            if (!columnExists(db, "users", "avatar_url")) {
                db.execSQL("ALTER TABLE users ADD COLUMN avatar_url TEXT");
            }
            db.execSQL("UPDATE users SET nickname = username WHERE nickname IS NULL");
        }

        if (oldVersion < 4) {
            if (!columnExists(db, "users", "password")) {
                db.execSQL("ALTER TABLE users ADD COLUMN password TEXT");
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
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

}
