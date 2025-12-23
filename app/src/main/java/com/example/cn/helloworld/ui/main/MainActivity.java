package com.example.cn.helloworld.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.entry.QuickEntryActivity;
import com.example.cn.helloworld.ui.search.ProductWebSearchActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;
import com.example.cn.helloworld.ui.user.UserProfileActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button browseButton = (Button) findViewById(R.id.button_browse_shops);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShopListActivity.class);
                startActivity(intent);
            }
        });

        View cartButton = findViewById(R.id.button_open_cart);
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        View cartSummaryButton = findViewById(R.id.button_cart_summary);
        cartSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        View searchView = findViewById(R.id.text_main_search);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductWebSearchActivity.class);
                startActivity(intent);
            }
        });

        setupQuickEntry(R.id.entry_flash_sale, R.string.home_action_flash_sale, R.string.home_action_flash_sale_desc);
        setupQuickEntry(R.id.entry_member, R.string.home_action_member, R.string.home_action_member_desc);
        setupQuickEntry(R.id.entry_coupon, R.string.home_action_coupon, R.string.home_action_coupon_desc);
        setupQuickEntry(R.id.entry_service, R.string.home_action_service, R.string.home_action_service_desc);

        View navHome = findViewById(R.id.nav_home);
        View navSearch = findViewById(R.id.nav_search);
        View navCart = findViewById(R.id.nav_cart);
        View navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stay on home
            }
        });
        navSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductWebSearchActivity.class);
                startActivity(intent);
            }
        });
        navCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupQuickEntry(int viewId, final int titleResId, final int descResId) {
        View entryView = findViewById(viewId);
        entryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QuickEntryActivity.class);
                intent.putExtra(QuickEntryActivity.EXTRA_TITLE_RES, titleResId);
                intent.putExtra(QuickEntryActivity.EXTRA_DESC_RES, descResId);
                startActivity(intent);
            }
        });
    }
}
