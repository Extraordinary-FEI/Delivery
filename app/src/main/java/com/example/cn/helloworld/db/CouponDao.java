package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CouponDao {
    private final DBHelper helper;

    public CouponDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public void insertCoupon(int userId, String couponName, int pointsCost) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("coupon_name", couponName);
        values.put("points_cost", pointsCost);
        values.put("created_at", System.currentTimeMillis());
        db.insert("user_coupons", null, values);
    }

    public int getCouponCount(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM user_coupons WHERE user_id=?",
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
}
