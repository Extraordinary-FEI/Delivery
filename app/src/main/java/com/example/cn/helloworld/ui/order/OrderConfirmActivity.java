package com.example.cn.helloworld.ui.order;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class OrderConfirmActivity extends BaseActivity {
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_ORDER_TIME = "extra_order_time";
    public static final String EXTRA_ORDER_ITEMS = "extra_order_items";
    public static final String EXTRA_ORDER_TOTAL = "extra_order_total";

    private static final String CART_PREFS = "cart_storage";

    private TextView orderIdView;
    private TextView orderTimeView;
    private TextView orderTotalView;
    private LinearLayout itemsContainer;
    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        orderIdView = (TextView) findViewById(R.id.order_id);
        orderTimeView = (TextView) findViewById(R.id.order_time);
        orderTotalView = (TextView) findViewById(R.id.order_total);
        itemsContainer = (LinearLayout) findViewById(R.id.items_container);
        payButton = (Button) findViewById(R.id.pay_button);

        bindOrderData();

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simulatePayment();
            }
        });
    }

    private void bindOrderData() {
        Intent intent = getIntent();
        String orderId = intent.getStringExtra(EXTRA_ORDER_ID);
        String orderTime = intent.getStringExtra(EXTRA_ORDER_TIME);
        ArrayList<String> items = intent.getStringArrayListExtra(EXTRA_ORDER_ITEMS);
        double total = intent.getDoubleExtra(EXTRA_ORDER_TOTAL, -1);

        if (orderId == null || orderId.trim().length() == 0) {
            orderId = "OD" + System.currentTimeMillis();
        }
        if (orderTime == null || orderTime.trim().length() == 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            orderTime = formatter.format(new Date());
        }
        if (items == null || items.isEmpty()) {
            items = new ArrayList<String>();
            items.add("黑椒牛排套餐 x1");
            items.add("经典意面 x1");
            items.add("冰柠檬茶 x2");
        }
        if (total < 0) {
            total = items.size() * 18.0;
        }

        orderIdView.setText("订单号：" + orderId);
        orderTimeView.setText("下单时间：" + orderTime);
        orderTotalView.setText(String.format(Locale.getDefault(), "总价：¥%.2f", total));

        itemsContainer.removeAllViews();
        for (String item : items) {
            itemsContainer.addView(createItemView(item));
        }
    }

    private TextView createItemView(String item) {
        TextView textView = new TextView(this);
        textView.setText(item);
        textView.setTextSize(15);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        int padding = (int) (getResources().getDisplayMetrics().density * 8);
        textView.setPadding(0, padding, 0, padding);
        return textView;
    }

    private void simulatePayment() {
        final boolean success = new Random().nextBoolean();
        String message = success ? "支付成功，感谢您的购买。" : "支付失败，请稍后重试。";
        String actionText = success ? "返回首页" : "知道了";

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("支付结果")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(actionText, null);

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (success) {
                    clearCart();
                    returnToHome();
                }
            }
        });
    }

    private void clearCart() {
        SharedPreferences preferences = getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    private void returnToHome() {
        Intent intent = new Intent();
        intent.setClassName(this, "com.example.cn.helloworld.ui.main.MainActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
