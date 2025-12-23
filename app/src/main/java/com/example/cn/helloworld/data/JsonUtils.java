package com.example.cn.helloworld.data;

import android.content.Context;

import com.example.cn.helloworld.model.Shop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class JsonUtils {
    private static final String SHOP_ASSET_PATH = "shops.json";

    private JsonUtils() {
    }

    public static List<Shop> loadShops(Context context) throws IOException, JSONException {
        String json = readAsset(context, SHOP_ASSET_PATH);
        JSONArray array = new JSONArray(json);
        List<Shop> shops = new ArrayList<Shop>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            Shop shop = new Shop(
                    item.getInt("id"),
                    item.getString("name"),
                    item.getString("address"),
                    item.getDouble("rating"),
                    item.getString("description"),
                    item.getString("phone"),
                    item.getString("imageUrl")
            );
            shops.add(shop);
        }
        return shops;
    }

    private static String readAsset(Context context, String assetPath) throws IOException {
        InputStream inputStream = context.getAssets().open(assetPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }
}
