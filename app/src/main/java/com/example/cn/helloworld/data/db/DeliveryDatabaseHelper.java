package com.example.cn.helloworld.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DeliveryDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "delivery.db";
    public static final int DATABASE_VERSION = 7;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_SHOPS = "shops";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_CART_ITEMS = "cart_items";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_ORDER_ITEMS = "order_items";

    public DeliveryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "role TEXT NOT NULL,"
                + "phone TEXT,"
                + "nickname TEXT,"
                + "avatar_url TEXT,"
                + "created_at INTEGER NOT NULL,"
                + "points INTEGER DEFAULT 0"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SHOPS + " ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT,"
                + "description TEXT,"
                + "address TEXT,"
                + "phone TEXT"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT,"
                + "shop_id TEXT"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + " ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT,"
                + "description TEXT,"
                + "price REAL,"
                + "image_url TEXT,"
                + "category_id TEXT,"
                + "shop_id TEXT,"
                + "available INTEGER DEFAULT 1"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CART_ITEMS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id TEXT,"
                + "product_id TEXT,"
                + "quantity INTEGER,"
                + "price REAL"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + " ("
                + "id TEXT PRIMARY KEY,"
                + "user_id TEXT,"
                + "shop_id TEXT,"
                + "total_amount REAL,"
                + "status TEXT,"
                + "created_at INTEGER"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_ITEMS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "order_id TEXT,"
                + "product_id TEXT,"
                + "quantity INTEGER,"
                + "price REAL"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS addresses ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "contact_name TEXT,"
                + "contact_phone TEXT,"
                + "detail TEXT,"
                + "is_default INTEGER DEFAULT 0,"
                + "created_at INTEGER"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS favorites ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "food_id TEXT NOT NULL,"
                + "food_name TEXT,"
                + "food_desc TEXT,"
                + "food_price REAL,"
                + "image_url TEXT,"
                + "created_at INTEGER,"
                + "UNIQUE(user_id, food_id) ON CONFLICT REPLACE"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS browse_history ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "food_id TEXT NOT NULL,"
                + "food_name TEXT,"
                + "food_desc TEXT,"
                + "food_price REAL,"
                + "image_url TEXT,"
                + "visited_at INTEGER,"
                + "UNIQUE(user_id, food_id) ON CONFLICT REPLACE"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS seckill ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "product_id TEXT NOT NULL,"
                + "seckill_price REAL NOT NULL,"
                + "stock INTEGER NOT NULL,"
                + "start_time INTEGER NOT NULL,"
                + "end_time INTEGER NOT NULL,"
                + "status INTEGER DEFAULT 1"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS user_coupons ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "coupon_name TEXT NOT NULL,"
                + "points_cost INTEGER NOT NULL,"
                + "created_at INTEGER"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS reviews ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "food_id TEXT NOT NULL,"
                + "food_name TEXT,"
                + "content TEXT,"
                + "created_at INTEGER"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTables(db);
        if (!columnExists(db, TABLE_USERS, "points")) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN points INTEGER DEFAULT 0");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTables(db);
    }

    private static boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        android.database.Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }
}
