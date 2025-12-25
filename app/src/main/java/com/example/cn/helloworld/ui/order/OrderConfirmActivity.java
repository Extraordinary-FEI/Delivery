package com.example.cn.helloworld.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.db.OrderDao;
import com.example.cn.helloworld.db.UserDao;
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
    public static final String EXTRA_USER_ID = "extra_user_id";

    private TextView orderIdView;
    private TextView orderTimeView;
    private TextView orderTotalView;
    private LinearLayout itemsContainer;
    private Button payButton;
    private Button reviewButton;
    private ArrayList<String> orderItems;
    private String orderId;
    private double orderTotal;
    private int userId;
    private OrderDao orderDao;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        setupBackButton();

        orderIdView = (TextView) findViewById(R.id.order_id);
        orderTimeView = (TextView) findViewById(R.id.order_time);
        orderTotalView = (TextView) findViewById(R.id.order_total);
        itemsContainer = (LinearLayout) findViewById(R.id.items_container);
        payButton = (Button) findViewById(R.id.pay_button);
        reviewButton = (Button) findViewById(R.id.button_review);
        orderDao = new OrderDao(this);
        userDao = new UserDao(this);

        bindOrderData();

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simulatePayment();
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReview();
            }
        });
    }

    private void bindOrderData() {
        Intent intent = getIntent();
        orderId = intent.getStringExtra(EXTRA_ORDER_ID);
        String orderTime = intent.getStringExtra(EXTRA_ORDER_TIME);
        orderItems = intent.getStringArrayListExtra(EXTRA_ORDER_ITEMS);
        orderTotal = intent.getDoubleExtra(EXTRA_ORDER_TOTAL, -1);
        userId = intent.getIntExtra(EXTRA_USER_ID, -1);

        if (orderId == null || orderId.trim().length() == 0) {
            orderId = "OD" + System.currentTimeMillis();
        }
        if (orderTime == null || orderTime.trim().length() == 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            orderTime = formatter.format(new Date());
        }
        if (orderItems == null || orderItems.isEmpty()) {
            orderItems = new ArrayList<String>();
            orderItems.add("黑椒牛排套餐 x1");
            orderItems.add("经典意面 x1");
            orderItems.add("冰柠檬茶 x2");
        }
        if (orderTotal < 0) {
            orderTotal = orderItems.size() * 18.0;
        }

        orderIdView.setText("订单号：" + orderId);
        orderTimeView.setText("下单时间：" + orderTime);
        orderTotalView.setText(String.format(Locale.getDefault(), "总价：¥%.2f", orderTotal));

        itemsContainer.removeAllViews();
        for (String item : orderItems) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("支付结果")
                .setMessage(message)
                .setCancelable(false);

        if (success) {
            builder.setPositiveButton("去评价", null);
            builder.setNegativeButton("返回首页", null);
        } else {
            builder.setPositiveButton("知道了", null);
        }

        final AlertDialog dialog = builder.create();
        dialog.show();
        if (success) {
            persistOrder();
            reviewButton.setEnabled(true);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    openReview();
                }
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    clearCart();
                    returnToHome();
                }
            });
        }
    }

    private void persistOrder() {
        ArrayList<OrderDao.OrderItem> items = new ArrayList<OrderDao.OrderItem>();
        for (String item : orderItems) {
            OrderDao.OrderItem orderItem = parseOrderItem(item);
            if (orderItem != null) {
                items.add(orderItem);
            }
        }
        orderDao.insertOrder(userId, orderId, orderTotal, items);
        int points = (int) Math.floor(orderTotal);
        userDao.addPoints(userId, points);
        clearCart();
    }

    private OrderDao.OrderItem parseOrderItem(String item) {
        if (item == null) {
            return null;
        }
        String[] parts = item.split(" x");
        String name = parts[0].trim();
        int quantity = 1;
        if (parts.length > 1) {
            try {
                quantity = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
                quantity = 1;
            }
        }
        return new OrderDao.OrderItem(name, quantity, 0);
    }

    private void openReview() {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putStringArrayListExtra(ReviewActivity.EXTRA_ITEMS, buildReviewItems());
        startActivity(intent);
    }

    private ArrayList<String> buildReviewItems() {
        ArrayList<String> reviewItems = new ArrayList<String>();
        if (orderItems == null) {
            return reviewItems;
        }
        for (String item : orderItems) {
            OrderDao.OrderItem parsed = parseOrderItem(item);
            if (parsed != null && !reviewItems.contains(parsed.productId)) {
                reviewItems.add(parsed.productId);
            }
        }
        return reviewItems;
    }

    private void clearCart() {
        CartManager.getInstance(this).clear();
    }

    private void returnToHome() {
        Intent intent = new Intent();
        intent.setClassName(this, "com.example.cn.helloworld.ui.main.MainActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
