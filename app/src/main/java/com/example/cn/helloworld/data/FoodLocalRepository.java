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
    private static final String SOURCE_SEED = "seed";
    private static final String SOURCE_USER = "user";

    public List<Food> getFoods(Context context) throws IOException {
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Food> foods = readFoodsFromDatabase(db, null);
        List<Food> assetFoods = readFoodsFromAssets(context);
        mergeMissingFoods(db, foods, assetFoods);
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
        upsertUserFood(db, food);
    }

    public void updateFood(Context context, Food updated) throws IOException {
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        upsertUserFood(db, updated);
    }

    public void updateFoodCategory(Context context, String foodId, String category) throws IOException {
        if (TextUtils.isEmpty(foodId)) {
            return;
        }
        DeliveryDatabaseHelper dbHelper = new DeliveryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String resolvedCategory = TextUtils.isEmpty(category) ? null : category;
        String shopId = "default";
        Cursor cursor = db.query(DeliveryDatabaseHelper.TABLE_PRODUCTS, new String[]{"shop_id"},
                "id = ?", new String[]{foodId}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                shopId = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        if (!TextUtils.isEmpty(resolvedCategory) && !categoryExists(db, resolvedCategory)) {
            ContentValues categoryValues = new ContentValues();
            categoryValues.put("id", resolvedCategory);
            categoryValues.put("name", resolvedCategory);
            categoryValues.put("shop_id", TextUtils.isEmpty(shopId) ? "default" : shopId);
            categoryValues.put("source", SOURCE_USER);
            categoryValues.put("updated_by_user", 1);
            db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_CATEGORIES, null, categoryValues,
                    SQLiteDatabase.CONFLICT_IGNORE);
        }
        ContentValues values = new ContentValues();
        if (TextUtils.isEmpty(resolvedCategory)) {
            values.putNull("category_id");
        } else {
            values.put("category_id", resolvedCategory);
        }
        values.put("source", SOURCE_USER);
        values.put("updated_by_user", 1);
        db.update(DeliveryDatabaseHelper.TABLE_PRODUCTS, values, "id = ?", new String[]{foodId});
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
        String json = LocalJsonStore.readAssetJson(context, FOOD_FILE);
        List<Food> foods = new Gson().fromJson(json, FOOD_LIST_TYPE);
        return foods == null ? new ArrayList<Food>() : foods;
    }

    public List<Food> getFoodsByCategory(Context context, String categoryId) throws IOException {
        List<Food> foods = getFoods(context);
        if (TextUtils.isEmpty(categoryId)) {
            return foods;
        }
        String defaultCategory = context.getString(com.example.cn.helloworld.R.string.category_unassigned);
        List<Food> filtered = new ArrayList<Food>();
        for (Food food : foods) {
            if (categoryId.equals(food.getCategory())
                    || (defaultCategory.equals(categoryId) && TextUtils.isEmpty(food.getCategory()))) {
                filtered.add(food);
            }
        }
        return filtered;
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

    private void mergeMissingFoods(SQLiteDatabase db, List<Food> existingFoods, List<Food> assetFoods) {
        if (assetFoods == null || assetFoods.isEmpty()) {
            return;
        }
        for (Food food : assetFoods) {
            if (food == null || TextUtils.isEmpty(food.getId())) {
                continue;
            }
            String seedId = food.getId();
            ProductMeta meta = findProductMeta(db, seedId);
            if (meta == null) {
                upsertSeedFood(db, food, seedId);
                existingFoods.add(food);
                continue;
            }
            if (meta.updatedByUser) {
                continue;
            }
            ContentValues values = new ContentValues();
            boolean changed = false;
            if (TextUtils.isEmpty(meta.seedId)) {
                values.put("seed_id", seedId);
                changed = true;
            }
            if (TextUtils.isEmpty(meta.source)) {
                values.put("source", SOURCE_SEED);
                changed = true;
            }
            if (TextUtils.isEmpty(meta.description) && !TextUtils.isEmpty(food.getDescription())) {
                values.put("description", food.getDescription());
                changed = true;
            }
            if (TextUtils.isEmpty(meta.imageUrl) && !TextUtils.isEmpty(food.getImageUrl())) {
                values.put("image_url", food.getImageUrl());
                changed = true;
            }
            if (TextUtils.isEmpty(meta.categoryId) && !TextUtils.isEmpty(food.getCategory())) {
                values.put("category_id", food.getCategory());
                changed = true;
                upsertCategory(db, food.getCategory(), food.getShopId(), SOURCE_SEED, false, food.getCategory());
            }
            if (changed) {
                db.update(DeliveryDatabaseHelper.TABLE_PRODUCTS, values, "id = ?",
                        new String[] { meta.id });
            }
        }
    }

    private void upsertUserFood(SQLiteDatabase db, Food food) {
        if (food == null || TextUtils.isEmpty(food.getId())) {
            return;
        }
        String seedId = getSeedId(db, food.getId());
        String source = getSource(db, food.getId());
        if (TextUtils.isEmpty(source)) {
            source = SOURCE_USER;
        }
        upsertFood(db, food, seedId, source, true);
    }

    private void upsertSeedFood(SQLiteDatabase db, Food food, String seedId) {
        if (food == null || TextUtils.isEmpty(food.getId())) {
            return;
        }
        upsertFood(db, food, seedId, SOURCE_SEED, false);
    }

    private void upsertFood(SQLiteDatabase db, Food food, String seedId, String source, boolean updatedByUser) {
        if (food == null || TextUtils.isEmpty(food.getId())) {
            return;
        }
        String category = TextUtils.isEmpty(food.getCategory()) ? null : food.getCategory();
        String shopId = TextUtils.isEmpty(food.getShopId()) ? "default" : food.getShopId();
        ContentValues shopValues = new ContentValues();
        shopValues.put("id", shopId);
        shopValues.put("name", "店铺 " + shopId);
        db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_SHOPS, null, shopValues,
                SQLiteDatabase.CONFLICT_IGNORE);

        if (!TextUtils.isEmpty(category)) {
            upsertCategory(db, category, shopId, source, updatedByUser, category);
        }

        ContentValues values = new ContentValues();
        values.put("id", food.getId());
        values.put("name", food.getName());
        values.put("description", food.getDescription());
        values.put("price", food.getPrice());
        values.put("image_url", food.getImageUrl());
        if (TextUtils.isEmpty(category)) {
            values.putNull("category_id");
        } else {
            values.put("category_id", category);
        }
        values.put("shop_id", shopId);
        values.put("available", 1);
        if (!TextUtils.isEmpty(seedId)) {
            values.put("seed_id", seedId);
        }
        values.put("source", source);
        values.put("updated_by_user", updatedByUser ? 1 : 0);
        db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_PRODUCTS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    private boolean categoryExists(SQLiteDatabase db, String name) {
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + DeliveryDatabaseHelper.TABLE_CATEGORIES + " WHERE name = ? LIMIT 1",
                new String[] { name });
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private void upsertCategory(SQLiteDatabase db, String category, String shopId, String source,
                                boolean updatedByUser, String seedId) {
        if (TextUtils.isEmpty(category)) {
            return;
        }
        String resolvedShopId = TextUtils.isEmpty(shopId) ? "default" : shopId;
        if (!categoryExists(db, category)) {
            ContentValues categoryValues = new ContentValues();
            categoryValues.put("id", category);
            categoryValues.put("name", category);
            categoryValues.put("shop_id", resolvedShopId);
            if (!TextUtils.isEmpty(seedId)) {
                categoryValues.put("seed_id", seedId);
            }
            categoryValues.put("source", source);
            categoryValues.put("updated_by_user", updatedByUser ? 1 : 0);
            db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_CATEGORIES, null, categoryValues,
                    SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private String getSeedId(SQLiteDatabase db, String id) {
        Cursor cursor = db.query(DeliveryDatabaseHelper.TABLE_PRODUCTS, new String[] { "seed_id" },
                "id = ?", new String[] { id }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private String getSource(SQLiteDatabase db, String id) {
        Cursor cursor = db.query(DeliveryDatabaseHelper.TABLE_PRODUCTS, new String[] { "source" },
                "id = ?", new String[] { id }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private ProductMeta findProductMeta(SQLiteDatabase db, String seedId) {
        Cursor cursor = db.query(DeliveryDatabaseHelper.TABLE_PRODUCTS,
                new String[] { "id", "seed_id", "source", "updated_by_user", "description", "image_url", "category_id" },
                "seed_id = ? OR id = ?", new String[] { seedId, seedId }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return new ProductMeta(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3) == 1,
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private static class ProductMeta {
        private final String id;
        private final String seedId;
        private final String source;
        private final boolean updatedByUser;
        private final String description;
        private final String imageUrl;
        private final String categoryId;

        private ProductMeta(String id, String seedId, String source, boolean updatedByUser,
                            String description, String imageUrl, String categoryId) {
            this.id = id;
            this.seedId = seedId;
            this.source = source;
            this.updatedByUser = updatedByUser;
            this.description = description;
            this.imageUrl = imageUrl;
            this.categoryId = categoryId;
        }
    }
}
