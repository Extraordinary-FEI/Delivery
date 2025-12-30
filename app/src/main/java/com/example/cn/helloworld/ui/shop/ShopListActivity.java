package com.example.cn.helloworld.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.data.ShopLocalRepository;
import com.example.cn.helloworld.model.Shop;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.entry.MemberCenterActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.ui.market.FoodMarketActivity;
import com.example.cn.helloworld.ui.shop.admin.ShopEditActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ShopListActivity extends BaseActivity implements ShopAdapter.OnShopClickListener {
    public static final String EXTRA_ALLOW_ADD_SHOP = "extra_allow_add_shop";
    private final List<Shop> shops = new ArrayList<Shop>();
    private ShopAdapter adapter;
    private final ShopLocalRepository repository = new ShopLocalRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);
        setupBackButton();

        Button addButton = (Button) findViewById(R.id.button_add_shop);
        if (SessionManager.isAdmin(this)) {
            addButton.setVisibility(Button.VISIBLE);
        }
        addButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent intent = new Intent(ShopListActivity.this, ShopEditActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_shops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShopAdapter(shops, this);
        recyclerView.setAdapter(adapter);

        setupDockActions();
        loadShopData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShopData();
    }

    private void loadShopData() {
        try {
            shops.clear();
            shops.addAll(repository.getShops(this));
            adapter.notifyDataSetChanged();
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_shop_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShopClick(Shop shop) {
        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ID, "shop_" + shop.getId());
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_NAME, shop.getName());
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ADDRESS, shop.getAddress());
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_RATING, (float) shop.getRating());
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_IMAGE, shop.getImageUrl());
        startActivity(intent);
    }

    private void setupDockActions() {
        findViewById(R.id.dock_home).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(ShopListActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.dock_delivery).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                // Already on delivery.
            }
        });

        findViewById(R.id.dock_market).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(ShopListActivity.this, FoodMarketActivity.class));
            }
        });

        findViewById(R.id.dock_cart).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(ShopListActivity.this, CartActivity.class));
            }
        });

        findViewById(R.id.dock_profile).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(ShopListActivity.this, MemberCenterActivity.class));
            }
        });
    }
}
