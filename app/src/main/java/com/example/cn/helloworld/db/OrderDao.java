package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.cn.helloworld.data.db.DeliveryDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    public static final int STATUS_PENDING_PAY = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_PACKING = 2;
    public static final int STATUS_DELIVERING = 3;
    public static final int STATUS_DELIVERED = 4;
    public static final int STATUS_CANCELLED = 5;

    private final DeliveryDatabaseHelper helper;

    public OrderDao(Context context) {
        helper = new DeliveryDatabaseHelper(context.getApplicationContext());
    }

    public void insertOrder(int userId, String orderId, double totalAmount, double payAmount,
                            String couponName, double couponDiscount, String addressDetail,
                            String contactName, String contactPhone, int status, List<OrderItem> items) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long now = System.currentTimeMillis();
        ContentValues orderValues = new ContentValues();
        orderValues.put("id", orderId);
        orderValues.put("user_id", String.valueOf(userId));
        orderValues.put("shop_id", "");
        orderValues.put("total_amount", totalAmount);
        orderValues.put("pay_amount", payAmount);
        orderValues.put("coupon_name", couponName);
        orderValues.put("coupon_discount", couponDiscount);
        orderValues.put("address_detail", addressDetail);
        orderValues.put("contact_name", contactName);
        orderValues.put("contact_phone", contactPhone);
        orderValues.put("status", status);
        orderValues.put("created_at", now);
        orderValues.put("updated_at", now);
        db.insertWithOnConflict(DeliveryDatabaseHelper.TABLE_ORDERS, null, orderValues,
                SQLiteDatabase.CONFLICT_REPLACE);

        if (items == null) {
            return;
        }
        for (OrderItem item : items) {
            ContentValues itemValues = new ContentValues();
            itemValues.put("order_id", orderId);
            itemValues.put("product_id", item.productId);
            itemValues.put("product_name", item.productName);
            itemValues.put("product_image_url", item.productImageUrl);
            itemValues.put("quantity", item.quantity);
            itemValues.put("price", item.price);
            itemValues.put("subtotal", item.subtotal);
            db.insert(DeliveryDatabaseHelper.TABLE_ORDER_ITEMS, null, itemValues);
        }
    }

    public boolean updateOrderStatus(String orderId, int status) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("updated_at", System.currentTimeMillis());
        if (status == STATUS_PAID) {
            values.put("paid_at", System.currentTimeMillis());
        }
        return db.update(DeliveryDatabaseHelper.TABLE_ORDERS, values, "id = ?",
                new String[]{orderId}) > 0;
    }

    public int getOrderStatus(String orderId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT status FROM " + DeliveryDatabaseHelper.TABLE_ORDERS + " WHERE id = ? LIMIT 1",
                new String[]{orderId});
        try {
            if (cursor.moveToFirst()) {
                return parseStatus(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        return STATUS_PENDING_PAY;
    }

    public List<OrderSummary> getOrdersForUser(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<OrderSummary> orders = new ArrayList<OrderSummary>();
        Cursor cursor = db.rawQuery(
                "SELECT id, total_amount, pay_amount, coupon_name, coupon_discount, status, created_at " +
                        "FROM " + DeliveryDatabaseHelper.TABLE_ORDERS + " WHERE user_id = ? " +
                        "ORDER BY created_at DESC",
                new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                orders.add(new OrderSummary(
                        cursor.getString(0),
                        cursor.getDouble(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        parseStatus(cursor.getString(5)),
                        cursor.getLong(6)));
            }
        } finally {
            cursor.close();
        }
        return orders;
    }

    public OrderDetail getOrderDetail(String orderId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, total_amount, pay_amount, coupon_name, coupon_discount, address_detail, " +
                        "contact_name, contact_phone, status, created_at, paid_at, updated_at " +
                        "FROM " + DeliveryDatabaseHelper.TABLE_ORDERS + " WHERE id = ? LIMIT 1",
                new String[]{orderId});
        try {
            if (cursor.moveToFirst()) {
                return new OrderDetail(
                        cursor.getString(0),
                        cursor.getDouble(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        parseStatus(cursor.getString(8)),
                        cursor.getLong(9),
                        cursor.getLong(10),
                        cursor.getLong(11));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public List<OrderItemDetail> getOrderItems(String orderId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<OrderItemDetail> items = new ArrayList<OrderItemDetail>();
        Cursor cursor = db.rawQuery(
                "SELECT product_id, product_name, product_image_url, quantity, price, subtotal " +
                        "FROM " + DeliveryDatabaseHelper.TABLE_ORDER_ITEMS + " WHERE order_id = ?",
                new String[]{orderId});
        try {
            while (cursor.moveToNext()) {
                items.add(new OrderItemDetail(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getDouble(4),
                        cursor.getDouble(5)));
            }
        } finally {
            cursor.close();
        }
        return items;
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
        public final String productName;
        public final String productImageUrl;
        public final int quantity;
        public final double price;
        public final double subtotal;

        public OrderItem(String productId, String productName, String productImageUrl,
                         int quantity, double price, double subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.productImageUrl = productImageUrl;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = subtotal;
        }
    }

    public static class OrderSummary {
        public final String orderId;
        public final double totalAmount;
        public final double payAmount;
        public final String couponName;
        public final double couponDiscount;
        public final int status;
        public final long createdAt;

        public OrderSummary(String orderId, double totalAmount, double payAmount, String couponName,
                            double couponDiscount, int status, long createdAt) {
            this.orderId = orderId;
            this.totalAmount = totalAmount;
            this.payAmount = payAmount;
            this.couponName = couponName;
            this.couponDiscount = couponDiscount;
            this.status = status;
            this.createdAt = createdAt;
        }
    }

    public static class OrderDetail {
        public final String orderId;
        public final double totalAmount;
        public final double payAmount;
        public final String couponName;
        public final double couponDiscount;
        public final String addressDetail;
        public final String contactName;
        public final String contactPhone;
        public final int status;
        public final long createdAt;
        public final long paidAt;
        public final long updatedAt;

        public OrderDetail(String orderId, double totalAmount, double payAmount, String couponName,
                           double couponDiscount, String addressDetail, String contactName,
                           String contactPhone, int status, long createdAt, long paidAt, long updatedAt) {
            this.orderId = orderId;
            this.totalAmount = totalAmount;
            this.payAmount = payAmount;
            this.couponName = couponName;
            this.couponDiscount = couponDiscount;
            this.addressDetail = addressDetail;
            this.contactName = contactName;
            this.contactPhone = contactPhone;
            this.status = status;
            this.createdAt = createdAt;
            this.paidAt = paidAt;
            this.updatedAt = updatedAt;
        }
    }

    public static class OrderItemDetail {
        public final String productId;
        public final String productName;
        public final String productImageUrl;
        public final int quantity;
        public final double price;
        public final double subtotal;

        public OrderItemDetail(String productId, String productName, String productImageUrl,
                               int quantity, double price, double subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.productImageUrl = productImageUrl;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = subtotal;
        }
    }

    private int parseStatus(String statusValue) {
        if (statusValue == null) {
            return STATUS_PENDING_PAY;
        }
        try {
            return Integer.parseInt(statusValue);
        } catch (NumberFormatException ignored) {
        }
        if ("paid".equalsIgnoreCase(statusValue)) {
            return STATUS_PAID;
        }
        if ("pending".equalsIgnoreCase(statusValue)) {
            return STATUS_PENDING_PAY;
        }
        if ("packing".equalsIgnoreCase(statusValue)) {
            return STATUS_PACKING;
        }
        if ("delivering".equalsIgnoreCase(statusValue)) {
            return STATUS_DELIVERING;
        }
        if ("delivered".equalsIgnoreCase(statusValue)) {
            return STATUS_DELIVERED;
        }
        if ("cancelled".equalsIgnoreCase(statusValue)) {
            return STATUS_CANCELLED;
        }
        return STATUS_PENDING_PAY;
    }
}
