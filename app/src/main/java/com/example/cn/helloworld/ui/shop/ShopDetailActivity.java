package com.example.cn.helloworld.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.data.ShopLocalRepository;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.model.Shop;
import com.example.cn.helloworld.ui.food.FoodDetailActivity;
import com.example.cn.helloworld.ui.shop.admin.FoodEditActivity;
import com.example.cn.helloworld.ui.shop.admin.ShopEditActivity;
import com.example.cn.helloworld.utils.ImageLoader;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ShopDetailActivity extends BaseActivity implements FoodAdapter.OnFoodClickListener {
    public static final String EXTRA_SHOP_ID = "extra_shop_id";
    public static final String EXTRA_SHOP_NAME = "extra_shop_name";
    public static final String EXTRA_SHOP_ADDRESS = "extra_shop_address";
    public static final String EXTRA_SHOP_RATING = "extra_shop_rating";
    public static final String EXTRA_SHOP_HOURS = "extra_shop_hours";
    public static final String EXTRA_SHOP = "extra_shop";
    public static final String EXTRA_SHOP_IMAGE = "extra_shop_image";

    private String shopId;
    private String shopName;
    private ImageView shopImage;
    private TextView nameView;
    private TextView addressView;
    private TextView ratingView;
    private TextView hoursView;
    private FoodAdapter adapter;
    private final List<Food> foods = new ArrayList<Food>();
    private final FoodLocalRepository foodRepository = new FoodLocalRepository();
    private final ShopLocalRepository shopRepository = new ShopLocalRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        setupBackButton();

        shopImage = (ImageView) findViewById(R.id.shop_detail_image);
        nameView = (TextView) findViewById(R.id.shop_detail_name);
        addressView = (TextView) findViewById(R.id.shop_detail_address);
        ratingView = (TextView) findViewById(R.id.shop_detail_rating);
        hoursView = (TextView) findViewById(R.id.shop_detail_hours);
        RecyclerView foodsView = (RecyclerView) findViewById(R.id.shop_detail_foods);
        Button editShopButton = (Button) findViewById(R.id.button_edit_shop);
        Button addFoodButton = (Button) findViewById(R.id.button_add_food);

        Intent intent = getIntent();
        shopId = intent.getStringExtra(EXTRA_SHOP_ID);
        shopName = intent.getStringExtra(EXTRA_SHOP_NAME);
        String address = intent.getStringExtra(EXTRA_SHOP_ADDRESS);
        float rating = intent.getFloatExtra(EXTRA_SHOP_RATING, 0f);
        String hours = intent.getStringExtra(EXTRA_SHOP_HOURS);
        String imageUrl = intent.getStringExtra(EXTRA_SHOP_IMAGE);
        Shop shop = (Shop) intent.getSerializableExtra(EXTRA_SHOP);

        if (shop != null) {
            shopId = String.valueOf(shop.getId());
            shopName = shop.getName();
            address = shop.getAddress();
            rating = (float) shop.getRating();
            imageUrl = shop.getImageUrl();
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

        bindShopInfo(shopName, address, rating, hours, imageUrl);

        foodsView.setLayoutManager(new LinearLayoutManager(this));
        if (SessionManager.isAdmin(this)) {
            adapter = new FoodAdapter(foods, this, new FoodAdapter.OnFoodLongClickListener() {
                @Override
                public void onFoodLongClick(Food food) {
                    Intent editIntent = new Intent(ShopDetailActivity.this, FoodEditActivity.class);
                    editIntent.putExtra(FoodEditActivity.EXTRA_FOOD_ID, food.getId());
                    editIntent.putExtra(FoodEditActivity.EXTRA_SHOP_ID, shopId);
                    startActivity(editIntent);
                }
            });
        } else {
            adapter = new FoodAdapter(foods, this);
        }
        foodsView.setAdapter(adapter);

        if (SessionManager.isAdmin(this)) {
            editShopButton.setVisibility(View.VISIBLE);
            addFoodButton.setVisibility(View.VISIBLE);
        }

        editShopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(ShopDetailActivity.this, ShopEditActivity.class);
                editIntent.putExtra(ShopEditActivity.EXTRA_SHOP_ID, shopId);
                startActivity(editIntent);
            }
        });

        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(ShopDetailActivity.this, FoodEditActivity.class);
                addIntent.putExtra(FoodEditActivity.EXTRA_SHOP_ID, shopId);
                startActivity(addIntent);
            }
        });

        loadFoods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShopInfo();
        loadFoods();
    }

    @Override
    public void onFoodClick(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_NAME, food.getName());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_PRICE, food.getPrice());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_DESCRIPTION, food.getDescription());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_IMAGE_URL, food.getImageUrl());
        startActivity(intent);
    }

    private void loadFoods() {
        try {
            foods.clear();
            foods.addAll(foodRepository.getFoodsForShop(this, shopId));
            adapter.notifyDataSetChanged();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void loadShopInfo() {
        if (TextUtils.isEmpty(shopId)) {
            return;
        }
        try {
            int id = parseShopId(shopId);
            Shop shop = shopRepository.getShopById(this, id);
            if (shop != null) {
                bindShopInfo(shop.getName(), shop.getAddress(), (float) shop.getRating(),
                        getString(R.string.default_shop_hours), shop.getImageUrl());
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void bindShopInfo(String name, String address, float rating, String hours, String imageUrl) {
        nameView.setText(name);
        addressView.setText(address);
        ratingView.setText(getString(R.string.shop_rating_format, rating));
        hoursView.setText(hours);
        ImageLoader.load(this, shopImage, imageUrl);
    }

    private int parseShopId(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return 0;
        }
        if (raw.startsWith("shop_")) {
            try {
                return Integer.parseInt(raw.substring(5));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
