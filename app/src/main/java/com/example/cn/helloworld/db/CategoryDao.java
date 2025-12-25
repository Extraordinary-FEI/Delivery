package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.cn.helloworld.data.db.DeliveryDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private final DeliveryDatabaseHelper helper;
    private final Context context;

    public CategoryDao(Context context) {
        this.context = context.getApplicationContext();
        helper = new DeliveryDatabaseHelper(this.context);
    }

    public List<String> getCategoryNames() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> result = new ArrayList<String>();
        String defaultCategory = getDefaultCategoryName();
        ensureCategoryExists(db, defaultCategory);
        result.add(defaultCategory);
        Cursor cursor = db.rawQuery(
                "SELECT name FROM " + DeliveryDatabaseHelper.TABLE_CATEGORIES + " ORDER BY name ASC",
                null);
        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                if (!TextUtils.isEmpty(name) && !"default".equals(name) && !defaultCategory.equals(name)) {
                    result.add(name);
                }
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public boolean insertCategory(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        if (name.equals(getDefaultCategoryName())) {
            return false;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + DeliveryDatabaseHelper.TABLE_CATEGORIES + " WHERE name = ? LIMIT 1",
                new String[]{name});
        try {
            if (cursor.moveToFirst()) {
                return false;
            }
        } finally {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put("id", "cat_" + System.currentTimeMillis());
        values.put("name", name);
        values.put("shop_id", "default");
        return db.insert(DeliveryDatabaseHelper.TABLE_CATEGORIES, null, values) > 0;
    }

    public boolean deleteCategory(String name) {
        if (TextUtils.isEmpty(name) || name.equals(getDefaultCategoryName())) {
            return false;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        String defaultCategory = getDefaultCategoryName();
        ensureCategoryExists(db, defaultCategory);
        ContentValues values = new ContentValues();
        values.put("category_id", defaultCategory);
        db.update(DeliveryDatabaseHelper.TABLE_PRODUCTS, values, "category_id = ?", new String[] { name });
        return db.delete(DeliveryDatabaseHelper.TABLE_CATEGORIES, "name = ?", new String[] { name }) > 0;
    }

    public String getDefaultCategoryName() {
        return context.getString(com.example.cn.helloworld.R.string.category_unassigned);
    }

    private void ensureCategoryExists(SQLiteDatabase db, String name) {
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + DeliveryDatabaseHelper.TABLE_CATEGORIES + " WHERE name = ? LIMIT 1",
                new String[] { name });
        try {
            if (cursor.moveToFirst()) {
                return;
            }
        } finally {
            cursor.close();
        }
        ContentValues values = new ContentValues();
        values.put("id", name);
        values.put("name", name);
        values.put("shop_id", "default");
        db.insert(DeliveryDatabaseHelper.TABLE_CATEGORIES, null, values);
    }
}
