package com.example.cn.helloworld.ui.market;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.entry.MemberCenterActivity;
import com.example.cn.helloworld.ui.food.FoodDetailActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;
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
    private TextView cartBadgeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        setupBackButton();
        setupDockActions();

        RecyclerView categoryList = (RecyclerView) findViewById(R.id.recycler_market_categories);
        categoryList.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categories, this);
        categoryList.setAdapter(categoryAdapter);

        RecyclerView foodList = (RecyclerView) findViewById(R.id.recycler_market_foods);
        foodList.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(visibleFoods, this, null, new FoodAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChange(int totalCount) {
                updateCartBadge(totalCount);
            }
        });
        foodList.setAdapter(foodAdapter);

        selectedCategory = getString(R.string.market_category_all);
        loadFoods();
        updateCartBadge(CartManager.getInstance(this).getTotalCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoods();
        updateCartBadge(CartManager.getInstance(this).getTotalCount());
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

    private void setupDockActions() {
        cartBadgeView = (TextView) findViewById(R.id.text_cart_badge);
        View searchButton = findViewById(R.id.button_market_search);
        View moreButton = findViewById(R.id.button_market_more);
        if (searchButton != null) {
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(FoodMarketActivity.this, R.string.market_action_search,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (moreButton != null) {
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(FoodMarketActivity.this, R.string.market_action_more,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.dock_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FoodMarketActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.dock_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FoodMarketActivity.this, ShopListActivity.class));
            }
        });

        findViewById(R.id.dock_market).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on market.
            }
        });

        findViewById(R.id.dock_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FoodMarketActivity.this, CartActivity.class));
            }
        });

        findViewById(R.id.dock_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FoodMarketActivity.this, MemberCenterActivity.class));
            }
        });
    }

    private void updateCartBadge(int count) {
        if (cartBadgeView == null) {
            return;
        }
        if (count <= 0) {
            cartBadgeView.setVisibility(View.GONE);
        } else {
            cartBadgeView.setVisibility(View.VISIBLE);
            cartBadgeView.setText(String.valueOf(count));
        }
    }
}
