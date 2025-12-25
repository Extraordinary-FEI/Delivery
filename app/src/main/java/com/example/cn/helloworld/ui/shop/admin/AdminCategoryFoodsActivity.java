package com.example.cn.helloworld.ui.shop.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.db.CategoryDao;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryFoodsActivity extends BaseActivity implements AdminFoodAdapter.OnFoodActionListener {
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private final List<Food> foods = new ArrayList<Food>();
    private final FoodLocalRepository repository = new FoodLocalRepository();
    private AdminFoodAdapter adapter;
    private String categoryName;
    private CategoryDao categoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category_foods);
        setupBackButton();

        categoryDao = new CategoryDao(this);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (TextUtils.isEmpty(categoryName)) {
            categoryName = getString(R.string.market_category_default);
        }

        TextView titleView = (TextView) findViewById(R.id.text_admin_category_foods_title);
        titleView.setText(getString(R.string.admin_category_foods_title, categoryName));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_admin_category_foods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminFoodAdapter(foods, this);
        recyclerView.setAdapter(adapter);

        loadFoods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoods();
    }

    @Override
    public void onEdit(Food food) {
        Intent intent = new Intent(this, FoodEditActivity.class);
        intent.putExtra(FoodEditActivity.EXTRA_FOOD_ID, food.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(final Food food) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_delete)
                .setMessage(R.string.confirm_delete_food)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFood(food);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onChangeCategory(final Food food) {
        if (food == null) {
            return;
        }
        final List<String> categoryNames = new ArrayList<String>();
        categoryNames.add(getString(R.string.food_category_unassigned));
        categoryNames.addAll(categoryDao.getCategoryNames());
        if (!TextUtils.isEmpty(food.getCategory()) && !categoryNames.contains(food.getCategory())) {
            categoryNames.add(food.getCategory());
        }
        if (categoryNames.size() <= 1) {
            Toast.makeText(this, R.string.admin_category_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        String currentCategory = food.getCategory();
        int checkedIndex = categoryNames.indexOf(currentCategory);
        if (checkedIndex < 0) {
            checkedIndex = 0;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_change_category)
                .setSingleChoiceItems(categoryNames.toArray(new String[0]), checkedIndex,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected = which == 0 ? null : categoryNames.get(which);
                                try {
                                    repository.updateFoodCategory(AdminCategoryFoodsActivity.this, food.getId(),
                                            selected);
                                    loadFoods();
                                    dialog.dismiss();
                                } catch (java.io.IOException e) {
                                    Toast.makeText(AdminCategoryFoodsActivity.this,
                                            R.string.error_food_save_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void loadFoods() {
        try {
            foods.clear();
            foods.addAll(repository.getFoodsByCategory(this, categoryName));
            adapter.notifyDataSetChanged();
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_food_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFood(Food food) {
        if (food == null) {
            return;
        }
        try {
            repository.deleteFood(this, food.getId());
            Toast.makeText(this, R.string.action_deleted, Toast.LENGTH_SHORT).show();
            loadFoods();
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_food_delete_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
