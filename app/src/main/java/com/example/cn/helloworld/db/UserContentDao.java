package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.cn.helloworld.model.Food;

import java.util.ArrayList;
import java.util.List;

public class UserContentDao {
    private final DBHelper helper;

    public UserContentDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public boolean isFavorite(int userId, String foodId) {
        if (TextUtils.isEmpty(foodId)) {
            return false;
        }
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "favorites",
                new String[]{"id"},
                "user_id = ? AND food_id = ?",
                new String[]{String.valueOf(userId), foodId},
                null,
                null,
                null,
                "1"
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public void toggleFavorite(int userId, Food food) {
        if (food == null) {
            return;
        }
        String foodId = resolveFoodId(food);
        if (isFavorite(userId, foodId)) {
            removeFavorite(userId, foodId);
        } else {
            addFavorite(userId, food);
        }
    }

    public void addFavorite(int userId, Food food) {
        if (food == null) {
            return;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = buildFoodValues(userId, food);
        values.put("created_at", System.currentTimeMillis());
        db.insertWithOnConflict("favorites", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        values.remove("created_at");
        values.put("visited_at", System.currentTimeMillis());
        db.insertWithOnConflict("browse_history", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeFavorite(int userId, String foodId) {
        if (TextUtils.isEmpty(foodId)) {
            return;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("favorites", "user_id = ? AND food_id = ?",
                new String[]{String.valueOf(userId), foodId});
        db.delete("browse_history", "user_id = ? AND food_id = ?",
                new String[]{String.valueOf(userId), foodId});
    }

    public List<Food> getFavorites(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "favorites",
                new String[]{"food_id", "food_name", "food_desc", "food_price", "image_url"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "created_at DESC"
        );
        List<Food> result = new ArrayList<Food>();
        try {
            while (cursor.moveToNext()) {
                result.add(mapFood(cursor));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public int getFavoriteCount(int userId) {
        return countForTable("favorites", userId);
    }

    public void addBrowseHistory(int userId, Food food) {
        if (food == null) {
            return;
        }
        if (!isFavorite(userId, resolveFoodId(food))) {
            return;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = buildFoodValues(userId, food);
        values.put("visited_at", System.currentTimeMillis());
        db.insertWithOnConflict("browse_history", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Food> getBrowseHistory(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT h.food_id, h.food_name, h.food_desc, h.food_price, h.image_url " +
                        "FROM browse_history h INNER JOIN favorites f " +
                        "ON h.user_id = f.user_id AND h.food_id = f.food_id " +
                        "WHERE h.user_id = ? " +
                        "ORDER BY h.visited_at DESC",
                new String[]{String.valueOf(userId)}
        );
        List<Food> result = new ArrayList<Food>();
        try {
            while (cursor.moveToNext()) {
                result.add(mapFood(cursor));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public int getHistoryCount(int userId) {
        return countForTable("browse_history", userId);
    }

    private int countForTable(String table, int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + table + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    private Food mapFood(Cursor cursor) {
        String id = cursor.getString(0);
        String name = cursor.getString(1);
        String desc = cursor.getString(2);
        double price = cursor.getDouble(3);
        String imageUrl = cursor.getString(4);
        return new Food(id, name, null, desc, price, imageUrl);
    }

    private ContentValues buildFoodValues(int userId, Food food) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("food_id", resolveFoodId(food));
        values.put("food_name", food.getName());
        values.put("food_desc", food.getDescription());
        values.put("food_price", food.getPrice());
        values.put("image_url", food.getImageUrl());
        return values;
    }

    private String resolveFoodId(Food food) {
        if (food == null) {
            return "";
        }
        if (!TextUtils.isEmpty(food.getId())) {
            return food.getId();
        }
        return food.getName();
    }
}
