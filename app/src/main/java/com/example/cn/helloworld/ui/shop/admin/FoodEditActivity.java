package com.example.cn.helloworld.ui.shop.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.db.CategoryDao;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class FoodEditActivity extends BaseActivity {
    public static final String EXTRA_SHOP_ID = "extra_shop_id";
    public static final String EXTRA_FOOD_ID = "extra_food_id";

    private final FoodLocalRepository repository = new FoodLocalRepository();
    private final List<String> categories = new ArrayList<String>();

    private EditText nameInput;
    private EditText priceInput;
    private EditText descriptionInput;
    private Spinner categoryInput;
    private EditText imageInput;
    private EditText shopIdInput;
    private ImageView previewImage;
    private String shopId;
    private String foodId;
    private String currentCategory;
    private ArrayAdapter<String> categoryAdapter;
    private CategoryDao categoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_edit);
        setupBackButton();

        shopIdInput = (EditText) findViewById(R.id.input_food_shop_id);
        nameInput = (EditText) findViewById(R.id.input_food_name);
        priceInput = (EditText) findViewById(R.id.input_food_price);
        descriptionInput = (EditText) findViewById(R.id.input_food_description);
        categoryInput = (Spinner) findViewById(R.id.input_food_category);
        imageInput = (EditText) findViewById(R.id.input_food_image);
        previewImage = (ImageView) findViewById(R.id.image_food_preview);
        categoryDao = new CategoryDao(this);
        categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryInput.setAdapter(categoryAdapter);
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

        loadCategories();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
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
            currentCategory = food.getCategory();
            setCategorySelection(currentCategory);
            imageInput.setText(food.getImageUrl());
            ImageLoader.load(this, previewImage, food.getImageUrl());
            shopId = food.getShopId();
            shopIdInput.setText(shopId);
            shopIdInput.setEnabled(false);
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_food_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCategories() {
        categories.clear();
        categories.addAll(categoryDao.getCategoryNames());
        if (!TextUtils.isEmpty(currentCategory) && !categories.contains(currentCategory)) {
            categories.add(currentCategory);
        }
        categoryAdapter.notifyDataSetChanged();
        setCategorySelection(currentCategory);
    }

    private void setCategorySelection(String category) {
        if (categories.isEmpty()) {
            return;
        }
        int index = categories.indexOf(category);
        if (index < 0) {
            index = 0;
        }
        categoryInput.setSelection(index);
    }

    private void saveFood() {
        String resolvedShopId = shopIdInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String priceValue = priceInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String category = null;
        int selectedIndex = categoryInput.getSelectedItemPosition();
        if (selectedIndex >= 0 && selectedIndex < categories.size()) {
            category = categories.get(selectedIndex);
        }
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

        Food food = new Food(foodId, name, resolvedShopId, description, price, imageUrl, category);
        try {
            repository.updateFood(this, food);
            Toast.makeText(this, R.string.action_saved, Toast.LENGTH_SHORT).show();
            finish();
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_food_save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
