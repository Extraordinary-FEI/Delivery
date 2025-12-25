package com.example.cn.helloworld.data;

import android.content.Context;

import com.example.cn.helloworld.model.Shop;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShopLocalRepository {
    private static final String SHOP_FILE = "shops.json";
    private static final Type SHOP_LIST_TYPE = new TypeToken<List<Shop>>() { } .getType();

    public List<Shop> getShops(Context context) throws IOException {
        String json = LocalJsonStore.readJson(context, SHOP_FILE);
        List<Shop> shops = new Gson().fromJson(json, SHOP_LIST_TYPE);
        return shops == null ? new ArrayList<Shop>() : shops;
    }

    public Shop getShopById(Context context, int shopId) throws IOException {
        List<Shop> shops = getShops(context);
        for (Shop shop : shops) {
            if (shop.getId() == shopId) {
                return shop;
            }
        }
        return null;
    }

    public void addShop(Context context, Shop shop) throws IOException {
        List<Shop> shops = getShops(context);
        shops.add(shop);
        writeShops(context, shops);
    }

    public void updateShop(Context context, Shop updated) throws IOException {
        List<Shop> shops = getShops(context);
        for (int i = 0; i < shops.size(); i++) {
            if (shops.get(i).getId() == updated.getId()) {
                shops.set(i, updated);
                writeShops(context, shops);
                return;
            }
        }
        shops.add(updated);
        writeShops(context, shops);
    }

    public boolean deleteShop(Context context, int shopId) throws IOException {
        List<Shop> shops = getShops(context);
        for (int i = 0; i < shops.size(); i++) {
            if (shops.get(i).getId() == shopId) {
                shops.remove(i);
                writeShops(context, shops);
                return true;
            }
        }
        return false;
    }

    private void writeShops(Context context, List<Shop> shops) throws IOException {
        String json = new Gson().toJson(shops, SHOP_LIST_TYPE);
        LocalJsonStore.writeJson(context, SHOP_FILE, json);
    }
}
