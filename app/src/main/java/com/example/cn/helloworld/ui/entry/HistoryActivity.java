package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserContentDao;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.food.FoodDetailActivity;
import com.example.cn.helloworld.ui.shop.FoodAdapter;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity implements FoodAdapter.OnFoodClickListener {
    private final List<Food> histories = new ArrayList<Food>();
    private FoodAdapter adapter;
    private UserContentDao contentDao;
    private TextView emptyView;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setupBackButton();

        userId = parseUserId(SessionManager.getUserId(this));
        contentDao = new UserContentDao(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_history);
        emptyView = (TextView) findViewById(R.id.text_history_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(histories, this);
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        histories.clear();
        histories.addAll(contentDao.getBrowseHistory(userId));
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(histories.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onFoodClick(Food food) {
        if (food == null) {
            return;
        }
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_ID, food.getId());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_NAME, food.getName());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_PRICE, food.getPrice());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_DESCRIPTION, food.getDescription());
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_IMAGE_URL, food.getImageUrl());
        intent.putExtra(FoodDetailActivity.EXTRA_SHOP_ID, food.getShopId());
        startActivity(intent);
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
