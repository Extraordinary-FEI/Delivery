package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.cn.helloworld.data.db.DeliveryDatabaseHelper;

import java.util.List;

public class OrderDao {
    private final DeliveryDatabaseHelper helper;

    public OrderDao(Context context) {
        helper = new DeliveryDatabaseHelper(context.getApplicationContext());
    }

    public void insertOrder(int userId, String orderId, double totalAmount, List<OrderItem> items) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues orderValues = new ContentValues();
        orderValues.put("id", orderId);
        orderValues.put("user_id", String.valueOf(userId));
        orderValues.put("shop_id", "");
        orderValues.put("total_amount", totalAmount);
        orderValues.put("status", "paid");
        orderValues.put("created_at", System.currentTimeMillis());
        db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_ORDERS, null, orderValues,
                SQLiteDatabase.CONFLICT_REPLACE);

        if (items == null) {
            return;
        }
        for (OrderItem item : items) {
            ContentValues itemValues = new ContentValues();
            itemValues.put("order_id", orderId);
            itemValues.put("product_id", item.productId);
            itemValues.put("quantity", item.quantity);
            itemValues.put("price", item.price);
            db.insert(DeliveryDatabaseHelper.TABLE_ORDER_ITEMS, null, itemValues);
        }
    }

    public boolean hasPurchasedProduct(int userId, String productId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + DeliveryDatabaseHelper.TABLE_ORDER_ITEMS + " oi " +
                        "INNER JOIN " + DeliveryDatabaseHelper.TABLE_ORDERS + " o " +
                        "ON oi.order_id = o.id " +
                        "WHERE o.user_id = ? AND oi.product_id = ? LIMIT 1",
                new String[]{String.valueOf(userId), productId}
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public static class OrderItem {
        public final String productId;
        public final int quantity;
        public final double price;

        public OrderItem(String productId, int quantity, double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
