package com.example.cn.helloworld.ui.market;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.food.FoodDetailActivity;
import com.example.cn.helloworld.ui.shop.FoodAdapter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FoodMarketActivity extends BaseActivity implements FoodAdapter.OnFoodClickListener,
        CategoryAdapter.OnCategoryClickListener {
    private final FoodLocalRepository repository = new FoodLocalRepository();
    private final List<Food> allFoods = new ArrayList<Food>();
    private final List<Food> visibleFoods = new ArrayList<Food>();
    private final List<String> categories = new ArrayList<String>();
    private FoodAdapter foodAdapter;
    private CategoryAdapter categoryAdapter;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        setupBackButton();

        RecyclerView categoryList = (RecyclerView) findViewById(R.id.recycler_market_categories);
        categoryList.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categories, this);
        categoryList.setAdapter(categoryAdapter);

        RecyclerView foodList = (RecyclerView) findViewById(R.id.recycler_market_foods);
        foodList.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(visibleFoods, this);
        foodList.setAdapter(foodAdapter);

        selectedCategory = getString(R.string.market_category_all);
        loadFoods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoods();
    }

    private void loadFoods() {
        try {
            allFoods.clear();
            allFoods.addAll(repository.getFoods(this));
            buildCategories();
            applyCategoryFilter();
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_food_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void buildCategories() {
        Set<String> categorySet = new LinkedHashSet<String>();
        categorySet.add(getString(R.string.market_category_all));
        for (Food food : allFoods) {
            categorySet.add(resolveCategory(food));
        }
        categories.clear();
        categories.addAll(categorySet);
        if (!categories.contains(selectedCategory)) {
            selectedCategory = categories.get(0);
        }
        categoryAdapter.setSelectedCategory(selectedCategory);
        categoryAdapter.notifyDataSetChanged();
    }

    private void applyCategoryFilter() {
        visibleFoods.clear();
        String allCategory = getString(R.string.market_category_all);
        for (Food food : allFoods) {
            String foodCategory = resolveCategory(food);
            if (allCategory.equals(selectedCategory) || selectedCategory.equals(foodCategory)) {
                visibleFoods.add(food);
            }
        }
        foodAdapter.notifyDataSetChanged();
    }

    private String resolveCategory(Food food) {
        if (food == null || TextUtils.isEmpty(food.getCategory())) {
            return getString(R.string.market_category_default);
        }
        return food.getCategory();
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

    @Override
    public void onCategoryClick(String category) {
        selectedCategory = category;
        categoryAdapter.setSelectedCategory(category);
        categoryAdapter.notifyDataSetChanged();
        applyCategoryFilter();
    }
}
