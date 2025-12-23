package com.example.cn.helloworld.ui.shop;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.model.Shop;
import com.example.cn.helloworld.ui.food.FoodDetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ShopDetailActivity extends BaseActivity implements FoodAdapter.OnFoodClickListener {
    public static final String EXTRA_SHOP_ID = "extra_shop_id";
    public static final String EXTRA_SHOP_NAME = "extra_shop_name";
    public static final String EXTRA_SHOP_ADDRESS = "extra_shop_address";
    public static final String EXTRA_SHOP_RATING = "extra_shop_rating";
    public static final String EXTRA_SHOP_HOURS = "extra_shop_hours";
    public static final String EXTRA_SHOP = "extra_shop";

    private String shopId;
    private String shopName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        setupBackButton();

        TextView nameView = (TextView) findViewById(R.id.shop_detail_name);
        TextView addressView = (TextView) findViewById(R.id.shop_detail_address);
        TextView ratingView = (TextView) findViewById(R.id.shop_detail_rating);
        TextView hoursView = (TextView) findViewById(R.id.shop_detail_hours);
        RecyclerView foodsView = (RecyclerView) findViewById(R.id.shop_detail_foods);

        Intent intent = getIntent();
        shopId = intent.getStringExtra(EXTRA_SHOP_ID);
        shopName = intent.getStringExtra(EXTRA_SHOP_NAME);
        String address = intent.getStringExtra(EXTRA_SHOP_ADDRESS);
        float rating = intent.getFloatExtra(EXTRA_SHOP_RATING, 0f);
        String hours = intent.getStringExtra(EXTRA_SHOP_HOURS);
        Shop shop = (Shop) intent.getSerializableExtra(EXTRA_SHOP);

        if (shop != null) {
            shopId = String.valueOf(shop.getId());
            shopName = shop.getName();
            address = shop.getAddress();
            rating = (float) shop.getRating();
        }

        if (TextUtils.isEmpty(shopId)) {
            shopId = "shop_1";
        }
        if (TextUtils.isEmpty(shopName)) {
            shopName = getString(R.string.default_shop_name);
        }
        if (TextUtils.isEmpty(address)) {
            address = getString(R.string.default_shop_address);
        }
        if (TextUtils.isEmpty(hours)) {
            hours = getString(R.string.default_shop_hours);
        }

        nameView.setText(shopName);
        addressView.setText(address);
        ratingView.setText(getString(R.string.shop_rating_format, rating));
        hoursView.setText(hours);

        foodsView.setLayoutManager(new LinearLayoutManager(this));
        FoodAdapter adapter = new FoodAdapter(loadFoodsForShop(this, shopId), this);
        foodsView.setAdapter(adapter);
    }

    @Override
    public void onFoodClick(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_NAME, food.getName());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_PRICE, food.getPrice());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_DESCRIPTION, food.getDescription());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_IMAGE_RES, food.getImageResId());
        startActivity(intent);
    }

    private List<Food> loadFoodsForShop(Context context, String targetShopId) {
        List<Food> foods = new ArrayList<Food>();
        String json = loadAssetText(context, "foods.json");
        if (TextUtils.isEmpty(json)) {
            return foods;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String shopId = item.optString("shopId");
                if (!TextUtils.equals(shopId, targetShopId)) {
                    continue;
                }
                int imageResId = resolveFoodImage(context, item.optString("image"));
                foods.add(new Food(
                        item.optString("id"),
                        item.optString("name"),
                        shopId,
                        item.optString("description"),
                        item.optDouble("price", 0),
                        imageResId
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return foods;
    }

    private int resolveFoodImage(Context context, String imageName) {
        if (TextUtils.isEmpty(imageName)) {
            return R.mipmap.ic_launcher;
        }
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId == 0) {
            return R.mipmap.ic_launcher;
        }
        return resId;
    }

    private String loadAssetText(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = assetManager.open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
