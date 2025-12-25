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

    public CategoryDao(Context context) {
        helper = new DeliveryDatabaseHelper(context.getApplicationContext());
    }

    public List<String> getCategoryNames() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> result = new ArrayList<String>();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM " + DeliveryDatabaseHelper.TABLE_CATEGORIES + " ORDER BY name ASC",
                null);
        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                if (!TextUtils.isEmpty(name)) {
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
}
