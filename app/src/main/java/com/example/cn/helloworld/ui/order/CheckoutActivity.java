package com.example.cn.helloworld.ui.order;

import android.os.Bundle;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.math.BigDecimal;

public class CheckoutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setupBackButton();

        TextView titleView = (TextView) findViewById(R.id.text_checkout_title);
        titleView.setText(R.string.checkout_title);

        CartManager cartManager = CartManager.getInstance(this);
        TextView totalCountView = (TextView) findViewById(R.id.text_checkout_total_count);
        TextView totalAmountView = (TextView) findViewById(R.id.text_checkout_total_amount);

        totalCountView.setText(getString(R.string.checkout_item_count_format, cartManager.getTotalCount()));
        BigDecimal totalAmount = BigDecimal.valueOf(cartManager.getTotalPrice());
        totalAmountView.setText(getString(R.string.cart_total_format, totalAmount.toPlainString()));
    }
}
