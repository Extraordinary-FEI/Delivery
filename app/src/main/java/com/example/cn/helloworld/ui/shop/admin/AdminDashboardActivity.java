package com.example.cn.helloworld.ui.shop.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;

public class AdminDashboardActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        setupBackButton();

        View shopManage = findViewById(R.id.card_manage_shops);
        shopManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ShopListActivity.class);
                intent.putExtra(ShopListActivity.EXTRA_ALLOW_ADD_SHOP, true);
                startActivity(intent);
            }
        });

        View foodManage = findViewById(R.id.card_manage_foods);
        foodManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ShopListActivity.class);
                intent.putExtra(ShopListActivity.EXTRA_ALLOW_ADD_SHOP, false);
                startActivity(intent);
            }
        });
    }
}
