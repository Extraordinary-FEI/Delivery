package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PointsDao {
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_SIGN = "SIGN";
    public static final String TYPE_REVIEW = "REVIEW";
    public static final String TYPE_INVITE = "INVITE";
    public static final String TYPE_REDEEM = "REDEEM";

    private final DBHelper helper;

    public PointsDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public int getPoints(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
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

    public boolean addPoints(int userId, int delta, String type, String refId, String remark) {
        if (delta <= 0) {
            return false;
        }
        return applyPointsChange(userId, delta, type, refId, remark);
    }

    public boolean deductPoints(int userId, int delta, String type, String refId, String remark) {
        if (delta <= 0) {
            return false;
        }
        return applyPointsChange(userId, -delta, type, refId, remark);
    }

    public List<PointsLog> listLogs(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<PointsLog> logs = new ArrayList<PointsLog>();
        Cursor cursor = db.rawQuery(
                "SELECT id, change, type, ref_id, remark, created_at FROM points_log " +
                        "WHERE user_id=? ORDER BY created_at DESC",
                new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                logs.add(new PointsLog(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5)
                ));
            }
        } finally {
            cursor.close();
        }
        return logs;
    }

    private boolean applyPointsChange(int userId, int delta, String type, String refId, String remark) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT points FROM users WHERE id=? LIMIT 1",
                new String[]{String.valueOf(userId)});
        int current = 0;
        try {
            if (cursor.moveToFirst()) {
                current = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        if (delta < 0 && current + delta < 0) {
            return false;
        }
        db.execSQL("UPDATE users SET points = points + ? WHERE id = ?",
                new Object[]{delta, userId});

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("change", delta);
        values.put("type", type);
        values.put("ref_id", refId);
        values.put("remark", remark);
        values.put("created_at", System.currentTimeMillis());
        db.insert("points_log", null, values);
        return true;
    }

    public static class PointsLog {
        public final int id;
        public final int change;
        public final String type;
        public final String refId;
        public final String remark;
        public final long createdAt;

        public PointsLog(int id, int change, String type, String refId, String remark, long createdAt) {
            this.id = id;
            this.change = change;
            this.type = type;
            this.refId = refId;
            this.remark = remark;
            this.createdAt = createdAt;
        }
    }
}
