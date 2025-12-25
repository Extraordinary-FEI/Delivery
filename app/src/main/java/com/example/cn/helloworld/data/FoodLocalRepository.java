package com.example.cn.helloworld.data;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.cn.helloworld.data.db.DeliveryDatabaseHelper;
import com.example.cn.helloworld.model.Food;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FoodLocalRepository {
    private static final String FOOD_FILE = "foods.json";
    private static final Type FOOD_LIST_TYPE = new TypeToken<List<Food>>() { } .getType();

    public List<Food> getFoods(Context context) throws IOException {
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Food> foods = readFoodsFromDatabase(db, null);
        if (!foods.isEmpty()) {
            return foods;
        }
        foods = readFoodsFromAssets(context);
        seedFoods(db, foods);
        return foods;
    }

    public List<Food> getFoodsForShop(Context context, String shopId) throws IOException {
        List<Food> foods = getFoods(context);
        if (TextUtils.isEmpty(shopId)) {
            return foods;
        }
        List<Food> filtered = new ArrayList<Food>();
        for (Food food : foods) {
            if (shopId.equals(food.getShopId())) {
                filtered.add(food);
            }
        }
        return filtered;
    }

    public Food getFoodById(Context context, String foodId) throws IOException {
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Food> foods = readFoodsFromDatabase(db, foodId);
        if (!foods.isEmpty()) {
            return foods.get(0);
        }
        List<Food> fallback = readFoodsFromAssets(context);
        for (Food food : fallback) {
            if (foodId.equals(food.getId())) {
                return food;
            }
        }
        return null;
    }

    public void addFood(Context context, Food food) throws IOException {
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        upsertFood(db, food);
    }

    public void updateFood(Context context, Food updated) throws IOException {
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        upsertFood(db, updated);
    }

    public void deleteFood(Context context, String foodId) throws IOException {
        if (TextUtils.isEmpty(foodId)) {
            return;
        }
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DeliveryDatabaseHelper.TABLE_PRODUCTS, "id = ?", new String[] { foodId });
    }

    private List<Food> readFoodsFromAssets(Context context) throws IOException {
        String json = LocalJsonStore.readJson(context, FOOD_FILE);
        List<Food> foods = new Gson().fromJson(json, FOOD_LIST_TYPE);
        return foods == null ? new ArrayList<Food>() : foods;
    }

    private List<Food> readFoodsFromDatabase(SQLiteDatabase db, String foodId) {
        List<Food> foods = new ArrayList<Food>();
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(foodId)) {
            selection = "id = ?";
            selectionArgs = new String[] { foodId };
        }
        Cursor cursor = db.query(DeliveryDatabaseHelper.TABLE_PRODUCTS,
                new String[] { "id", "name", "shop_id", "description", "price", "image_url", "category_id" },
                selection, selectionArgs, null, null, null);
        try {
            while (cursor.moveToNext()) {
                foods.add(new Food(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getString(5),
                        cursor.getString(6)));
            }
        } finally {
            cursor.close();
        }
        return foods;
    }

    private void seedFoods(SQLiteDatabase db, List<Food> foods) {
        if (foods == null || foods.isEmpty()) {
            return;
        }
        for (Food food : foods) {
            upsertFood(db, food);
        }
    }

    private void upsertFood(SQLiteDatabase db, Food food) {
        if (food == null || TextUtils.isEmpty(food.getId())) {
            return;
        }
        String category = TextUtils.isEmpty(food.getCategory()) ? "default" : food.getCategory();
        String shopId = TextUtils.isEmpty(food.getShopId()) ? "default" : food.getShopId();
        ContentValues shopValues = new ContentValues();
        shopValues.put("id", shopId);
        shopValues.put("name", "店铺 " + shopId);
        db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_SHOPS, null, shopValues,
                SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues categoryValues = new ContentValues();
        categoryValues.put("id", category);
        categoryValues.put("name", category);
        categoryValues.put("shop_id", shopId);
        db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_CATEGORIES, null, categoryValues,
                SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues values = new ContentValues();
        values.put("id", food.getId());
        values.put("name", food.getName());
        values.put("description", food.getDescription());
        values.put("price", food.getPrice());
        values.put("image_url", food.getImageUrl());
        values.put("category_id", category);
        values.put("shop_id", shopId);
        values.put("available", 1);
        db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_PRODUCTS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }
}
