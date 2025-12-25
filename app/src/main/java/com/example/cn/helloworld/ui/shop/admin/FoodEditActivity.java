package com.example.cn.helloworld.ui.shop.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.ImageLoader;

public class FoodEditActivity extends BaseActivity {
    public static final String EXTRA_SHOP_ID = "extra_shop_id";
    public static final String EXTRA_FOOD_ID = "extra_food_id";

    private final FoodLocalRepository repository = new FoodLocalRepository();

    private EditText nameInput;
    private EditText priceInput;
    private EditText descriptionInput;
    private EditText imageInput;
    private EditText shopIdInput;
    private ImageView previewImage;
    private String shopId;
    private String foodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_edit);
        setupBackButton();

        shopIdInput = (EditText) findViewById(R.id.input_food_shop_id);
        nameInput = (EditText) findViewById(R.id.input_food_name);
        priceInput = (EditText) findViewById(R.id.input_food_price);
        descriptionInput = (EditText) findViewById(R.id.input_food_description);
        imageInput = (EditText) findViewById(R.id.input_food_image);
        previewImage = (ImageView) findViewById(R.id.image_food_preview);
        imageInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ImageLoader.load(FoodEditActivity.this, previewImage,
                            imageInput.getText().toString());
                }
            }
        });

        Button saveButton = (Button) findViewById(R.id.button_save_food);

        shopId = getIntent().getStringExtra(EXTRA_SHOP_ID);
        foodId = getIntent().getStringExtra(EXTRA_FOOD_ID);

        if (!TextUtils.isEmpty(foodId)) {
            loadFood(foodId);
        }

        if (!TextUtils.isEmpty(shopId)) {
            shopIdInput.setText(shopId);
            shopIdInput.setEnabled(false);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFood();
            }
        });
    }

    private void loadFood(String id) {
        try {
            Food food = repository.getFoodById(this, id);
            if (food == null) {
                return;
            }
            nameInput.setText(food.getName());
            priceInput.setText(String.valueOf(food.getPrice()));
            descriptionInput.setText(food.getDescription());
            imageInput.setText(food.getImageUrl());
            ImageLoader.load(this, previewImage, food.getImageUrl());
            shopId = food.getShopId();
            shopIdInput.setText(shopId);
            shopIdInput.setEnabled(false);
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_food_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFood() {
        String resolvedShopId = shopIdInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String priceValue = priceInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String imageUrl = imageInput.getText().toString().trim();

        if (TextUtils.isEmpty(resolvedShopId)) {
            resolvedShopId = shopId;
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceValue) || TextUtils.isEmpty(resolvedShopId)) {
            Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceValue);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_invalid_price, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(foodId)) {
            foodId = "food_" + System.currentTimeMillis();
        }

        Food food = new Food(foodId, name, resolvedShopId, description, price, imageUrl);
        try {
            repository.updateFood(this, food);
            Toast.makeText(this, R.string.action_saved, Toast.LENGTH_SHORT).show();
            finish();
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_food_save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
