package com.example.cn.helloworld.data;

import android.content.Context;

import com.example.cn.helloworld.data.model.Shop;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class JsonLoader {
    private static final String SHOP_FILE = "shops.json";

    public static List<Shop> loadShops(Context context) {
        String json = readAsset(context, SHOP_FILE);
        if (json == null) {
            return Collections.<Shop>emptyList();
        }
        Type type = new TypeToken<List<Shop>>() { } .getType();
        return new Gson().fromJson(json, type);
    }

    private static String readAsset(Context context, String filename) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
