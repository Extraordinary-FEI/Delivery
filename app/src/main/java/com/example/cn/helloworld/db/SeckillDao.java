package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.data.db.DeliveryDatabaseHelper;
import com.example.cn.helloworld.data.model.SeckillItem;
import com.example.cn.helloworld.model.Food;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SeckillDao {
    private final DeliveryDatabaseHelper helper;
    private final FoodLocalRepository foodRepository;

    public SeckillDao(Context context) {
        helper = new DeliveryDatabaseHelper(context.getApplicationContext());
        foodRepository = new FoodLocalRepository();
    }

    public List<SeckillItem> getSeckillItems(Context context) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "seckill",
                new String[]{"id", "product_id", "seckill_price", "stock", "start_time", "end_time", "status"},
                null,
                null,
                null,
                null,
                "start_time ASC"
        );
        List<SeckillItem> items = new ArrayList<SeckillItem>();
        try {
            while (cursor.moveToNext()) {
                String productId = cursor.getString(1);
                Food food = null;
                try {
                    food = foodRepository.getFoodById(context, productId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                items.add(new SeckillItem(
                        cursor.getLong(0),
                        productId,
                        cursor.getDouble(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getInt(6),
                        food
                ));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public void insertOrUpdate(SeckillItem item) {
        if (item == null || TextUtils.isEmpty(item.productId)) {
            return;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("product_id", item.productId);
        values.put("seckill_price", item.seckillPrice);
        values.put("stock", item.stock);
        values.put("start_time", item.startTime);
        values.put("end_time", item.endTime);
        values.put("status", item.status);
        if (item.id > 0) {
            db.update("seckill", values, "id = ?", new String[]{String.valueOf(item.id)});
        } else {
            db.insert("seckill", null, values);
        }
    }

    public void updateStatus(long id, int status) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("seckill", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("seckill", "id = ?", new String[]{String.valueOf(id)});
    }

    public boolean decreaseStock(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT stock FROM seckill WHERE id = ? LIMIT 1",
                new String[]{String.valueOf(id)});
        int stock = 0;
        try {
            if (cursor.moveToFirst()) {
                stock = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        if (stock <= 0) {
            return false;
        }
        db.execSQL("UPDATE seckill SET stock = stock - 1 WHERE id = ? AND stock > 0",
                new Object[]{id});
        return true;
    }

    public void seedDefaults(Context context) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM seckill", null);
        try {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                return;
            }
        } finally {
            cursor.close();
        }
        try {
            List<Food> foods = foodRepository.getFoods(context);
            long now = System.currentTimeMillis();
            for (int i = 0; i < Math.min(2, foods.size()); i++) {
                Food food = foods.get(i);
                double seckillPrice = Math.max(1, food.getPrice() * 0.7);
                insertOrUpdate(new SeckillItem(
                        0,
                        food.getId(),
                        seckillPrice,
                        20 - i * 5,
                        now,
                        now + 2 * 60 * 60 * 1000,
                        1,
                        food
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

