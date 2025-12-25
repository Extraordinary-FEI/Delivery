package com.example.cn.helloworld.data.cart;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREFS_NAME = "food_cart";
    private static final String KEY_ITEMS = "items";

    private static CartManager instance;

    private final SharedPreferences sharedPreferences;
    private final List<CartItem> items = new ArrayList<CartItem>();

    private CartManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadFromPreferences();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public synchronized void addItem(FoodItem foodItem) {
        CartItem existing = findItem(foodItem.getName());
        if (existing == null) {
            items.add(new CartItem(foodItem.getName(), foodItem.getPrice(), 1));
        } else {
            existing.increaseQuantity(1);
        }
        saveToPreferences();
    }

    public synchronized List<CartItem> getItems() {
        return new ArrayList<CartItem>(items);
    }

    public synchronized void updateItemQuantity(String name, int quantity) {
        CartItem item = findItem(name);
        if (item == null) {
            return;
        }
        if (quantity <= 0) {
            items.remove(item);
        } else {
            item.setQuantity(quantity);
        }
        saveToPreferences();
    }

    public synchronized void removeItem(String name) {
        CartItem item = findItem(name);
        if (item != null) {
            items.remove(item);
            saveToPreferences();
        }
    }

    public synchronized int getTotalCount() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public synchronized int getItemQuantity(String name) {
        CartItem item = findItem(name);
        return item == null ? 0 : item.getQuantity();
    }

    public synchronized double getTotalPrice() {
        double total = 0.0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    private CartItem findItem(String name) {
        for (CartItem item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    private void loadFromPreferences() {
        String json = sharedPreferences.getString(KEY_ITEMS, "[]");
        items.clear();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("name");
                double price = obj.optDouble("price", 0.0);
                int quantity = obj.optInt("quantity", 0);
                if (quantity > 0) {
                    items.add(new CartItem(name, price, quantity));
                }
            }
        } catch (JSONException ignored) {
        }
    }

    private void saveToPreferences() {
        JSONArray array = new JSONArray();
        for (CartItem item : items) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", item.getName());
                obj.put("price", item.getPrice());
                obj.put("quantity", item.getQuantity());
                array.put(obj);
            } catch (JSONException ignored) {
            }
        }
        sharedPreferences.edit().putString(KEY_ITEMS, array.toString()).apply();
    }
}
