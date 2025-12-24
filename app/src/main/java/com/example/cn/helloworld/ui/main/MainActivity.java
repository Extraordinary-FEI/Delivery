package com.example.cn.helloworld.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.entry.CouponCenterActivity;
import com.example.cn.helloworld.ui.entry.FlashSaleActivity;
import com.example.cn.helloworld.ui.entry.MemberCenterActivity;
import com.example.cn.helloworld.ui.entry.ServiceHelpActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;
import com.example.cn.helloworld.ui.shop.admin.AdminDashboardActivity;
import com.example.cn.helloworld.utils.SessionManager;

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

        View flashSaleEntry = findViewById(R.id.entry_flash_sale);
        flashSaleEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FlashSaleActivity.class));
            }
        });

        View memberEntry = findViewById(R.id.entry_member_center);
        memberEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MemberCenterActivity.class));
            }
        });

        View couponEntry = findViewById(R.id.entry_coupon_center);
        couponEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CouponCenterActivity.class));
            }
        });

        View serviceEntry = findViewById(R.id.entry_service_help);
        serviceEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ServiceHelpActivity.class));
            }
        });

        Button adminButton = (Button) findViewById(R.id.button_admin_dashboard);
        if (SessionManager.isAdmin(this)) {
            adminButton.setVisibility(View.VISIBLE);
        }
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
            }
        });
    }
}
