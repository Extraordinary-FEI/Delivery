package com.example.cn.helloworld.data;

import android.content.Context;

import com.example.cn.helloworld.data.model.Shop;

import java.util.List;

public class ShopRepository {
    private static List<Shop> cached;

    public static List<Shop> getShops(Context context) {
        if (cached == null) {
            cached = JsonLoader.loadShops(context);
        }
        return cached;
    }

    public static Shop getShopById(Context context, String shopId) {
        List<Shop> shops = getShops(context);
        for (Shop shop : shops) {
            if (shop.getId().equals(shopId)) {
                return shop;
            }
        }
        return null;
    }
}
