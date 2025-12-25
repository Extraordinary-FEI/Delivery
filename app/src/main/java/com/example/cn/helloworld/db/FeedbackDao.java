package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class FeedbackDao {
    public static final String TYPE_COMPLAINT = "COMPLAINT";
    public static final String TYPE_FEEDBACK = "FEEDBACK";

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_DONE = 2;

    private final DBHelper helper;

    public FeedbackDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public long insertFeedback(int userId, String type, String content) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("type", type);
        values.put("content", content);
        values.put("status", STATUS_PENDING);
        values.put("created_at", System.currentTimeMillis());
        values.put("updated_at", System.currentTimeMillis());
        return db.insert("feedback", null, values);
    }

    public List<FeedbackItem> listFeedback(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<FeedbackItem> items = new ArrayList<FeedbackItem>();
        Cursor cursor = db.rawQuery(
                "SELECT id, type, content, status, created_at, updated_at, reply " +
                        "FROM feedback WHERE user_id=? ORDER BY created_at DESC",
                new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                items.add(new FeedbackItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getString(6)
                ));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public FeedbackItem getFeedbackById(int feedbackId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, type, content, status, created_at, updated_at, reply " +
                        "FROM feedback WHERE id=? LIMIT 1",
                new String[]{String.valueOf(feedbackId)});
        try {
            if (cursor.moveToFirst()) {
                return new FeedbackItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getString(6)
                );
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public static class FeedbackItem {
        public final int id;
        public final String type;
        public final String content;
        public final int status;
        public final long createdAt;
        public final long updatedAt;
        public final String reply;

        public FeedbackItem(int id, String type, String content, int status, long createdAt,
                            long updatedAt, String reply) {
            this.id = id;
            this.type = type;
            this.content = content;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.reply = reply;
        }
    }
}

