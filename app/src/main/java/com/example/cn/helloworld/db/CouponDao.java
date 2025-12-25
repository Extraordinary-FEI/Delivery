package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

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

    public List<Coupon> getCoupons(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Coupon> coupons = new ArrayList<Coupon>();
        Cursor cursor = db.rawQuery(
                "SELECT coupon_name, points_cost, created_at FROM user_coupons WHERE user_id=? " +
                        "ORDER BY created_at DESC",
                new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                coupons.add(new Coupon(cursor.getString(0), cursor.getInt(1), cursor.getLong(2)));
            }
        } finally {
            cursor.close();
        }
        return coupons;
    }

    public static class Coupon {
        public final String name;
        public final int pointsCost;
        public final long createdAt;

        public Coupon(String name, int pointsCost, long createdAt) {
            this.name = name;
            this.pointsCost = pointsCost;
            this.createdAt = createdAt;
        }
    }
}
