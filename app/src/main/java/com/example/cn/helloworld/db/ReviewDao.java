package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
    private final DBHelper helper;

    public ReviewDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public void addReview(int userId, String foodId, String foodName, String content) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("food_id", foodId);
        values.put("food_name", foodName);
        values.put("content", content);
        values.put("created_at", System.currentTimeMillis());
        db.insert("reviews", null, values);
    }

    public List<Review> getReviewsForFood(String foodId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "reviews",
                new String[]{"food_name", "content", "created_at"},
                "food_id = ?",
                new String[]{foodId},
                null,
                null,
                "created_at DESC"
        );
        List<Review> reviews = new ArrayList<Review>();
        try {
            while (cursor.moveToNext()) {
                reviews.add(new Review(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getLong(2)
                ));
            }
        } finally {
            cursor.close();
        }
        return reviews;
    }

    public static class Review {
        public final String foodName;
        public final String content;
        public final long createdAt;

        public Review(String foodName, String content, long createdAt) {
            this.foodName = foodName;
            this.content = content;
            this.createdAt = createdAt;
        }
    }
}

