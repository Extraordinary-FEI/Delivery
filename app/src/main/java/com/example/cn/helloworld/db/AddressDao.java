package com.example.cn.helloworld.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.cn.helloworld.data.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressDao {
    private final DBHelper helper;

    public AddressDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public List<Address> getAddresses(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "addresses",
                new String[]{"id", "user_id", "contact_name", "contact_phone", "province", "city", "district", "detail", "is_default", "created_at"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "is_default DESC, created_at DESC"
        );
        List<Address> result = new ArrayList<Address>();
        try {
            while (cursor.moveToNext()) {
                result.add(mapAddress(cursor));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public Address getAddress(int userId, int addressId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "addresses",
                new String[]{"id", "user_id", "contact_name", "contact_phone", "province", "city", "district", "detail", "is_default", "created_at"},
                "user_id = ? AND id = ?",
                new String[]{String.valueOf(userId), String.valueOf(addressId)},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return mapAddress(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public Address getDefaultAddress(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "addresses",
                new String[]{"id", "user_id", "contact_name", "contact_phone", "province", "city", "district", "detail", "is_default", "created_at"},
                "user_id = ? AND is_default = 1",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "created_at DESC",
                "1"
        );
        try {
            if (cursor.moveToFirst()) {
                return mapAddress(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public int saveAddress(Address address) {
        if (address == null) {
            return -1;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", address.getUserId());
        values.put("contact_name", address.getContactName());
        values.put("contact_phone", address.getContactPhone());
        values.put("province", address.getProvince());
        values.put("city", address.getCity());
        values.put("district", address.getDistrict());
        values.put("detail", address.getDetail());
        values.put("is_default", address.isDefault() ? 1 : 0);
        values.put("created_at", address.getCreatedAt());

        if (address.getId() > 0) {
            db.update("addresses", values, "id = ? AND user_id = ?",
                    new String[]{String.valueOf(address.getId()), String.valueOf(address.getUserId())});
            if (address.isDefault()) {
                setDefaultAddress(address.getUserId(), address.getId());
            }
            return address.getId();
        }
        long id = db.insert("addresses", null, values);
        if (address.isDefault() || !hasDefaultAddress(address.getUserId())) {
            setDefaultAddress(address.getUserId(), (int) id);
        }
        return (int) id;
    }

    public boolean deleteAddress(int userId, int addressId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("addresses", "id = ? AND user_id = ?",
                new String[]{String.valueOf(addressId), String.valueOf(userId)}) > 0;
    }

    public void setDefaultAddress(int userId, int addressId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues reset = new ContentValues();
        reset.put("is_default", 0);
        db.update("addresses", reset, "user_id = ?", new String[]{String.valueOf(userId)});

        ContentValues update = new ContentValues();
        update.put("is_default", 1);
        db.update("addresses", update, "id = ? AND user_id = ?",
                new String[]{String.valueOf(addressId), String.valueOf(userId)});
    }

    private Address mapAddress(Cursor cursor) {
        return new Address(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getInt(8) == 1,
                cursor.getLong(9)
        );
    }

    private boolean hasDefaultAddress(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM addresses WHERE user_id = ? AND is_default = 1 LIMIT 1",
                new String[]{String.valueOf(userId)}
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }
}
