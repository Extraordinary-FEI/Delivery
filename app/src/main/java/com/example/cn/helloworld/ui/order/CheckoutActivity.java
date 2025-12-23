package com.example.cn.helloworld.ui.order;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.cn.helloworld.R;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        TextView titleView = (TextView) findViewById(R.id.text_checkout_title);
        titleView.setText(R.string.checkout_title);
    }
}

