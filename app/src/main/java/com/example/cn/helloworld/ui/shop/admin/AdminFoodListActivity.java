package com.example.cn.helloworld.ui.shop.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminFoodListActivity extends BaseActivity implements AdminFoodAdapter.OnFoodActionListener {
    private final List<Food> foods = new ArrayList<Food>();
    private final FoodLocalRepository repository = new FoodLocalRepository();
    private AdminFoodAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_food_list);
        setupBackButton();

        Button addFoodButton = (Button) findViewById(R.id.button_add_food);
        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminFoodListActivity.this, FoodEditActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_admin_foods);
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

    private void loadFoods() {
        try {
            foods.clear();
            foods.addAll(repository.getFoods(this));
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
