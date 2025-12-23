package com.example.cn.helloworld.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;

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
    }
}
