package com.example.cn.helloworld.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.OrderDao;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderConfirmActivity extends BaseActivity {
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_ORDER_TIME = "extra_order_time";
    public static final String EXTRA_ORDER_ITEMS = "extra_order_items";
    public static final String EXTRA_ORDER_TOTAL = "extra_order_total";
    public static final String EXTRA_USER_ID = "extra_user_id";

    private TextView orderIdView;
    private TextView orderTimeView;
    private TextView orderTotalView;
    private TextView orderCouponView;
    private TextView orderPayAmountView;
    private TextView orderStatusView;
    private TextView orderAddressView;
    private TextView orderContactView;
    private TextView stepPendingView;
    private TextView stepPackingView;
    private TextView stepDeliveringView;
    private TextView stepDeliveredView;
    private View lineStepOne;
    private View lineStepTwo;
    private View lineStepThree;
    private LinearLayout itemsContainer;
    private Button payButton;
    private Button confirmReceiveButton;
    private Button reviewButton;
    private ArrayList<String> orderItems;
    private String orderId;
    private double orderTotal;
    private int userId;
    private OrderDao orderDao;
    private UserDao userDao;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        setupBackButton();

        orderIdView = (TextView) findViewById(R.id.order_id);
        orderTimeView = (TextView) findViewById(R.id.order_time);
        orderTotalView = (TextView) findViewById(R.id.order_total);
        orderCouponView = (TextView) findViewById(R.id.order_coupon);
        orderPayAmountView = (TextView) findViewById(R.id.order_pay_amount);
        orderStatusView = (TextView) findViewById(R.id.text_order_status);
        orderAddressView = (TextView) findViewById(R.id.text_order_address);
        orderContactView = (TextView) findViewById(R.id.text_order_contact);
        stepPendingView = (TextView) findViewById(R.id.text_step_pending);
        stepPackingView = (TextView) findViewById(R.id.text_step_packing);
        stepDeliveringView = (TextView) findViewById(R.id.text_step_delivering);
        stepDeliveredView = (TextView) findViewById(R.id.text_step_delivered);
        lineStepOne = findViewById(R.id.line_step_one);
        lineStepTwo = findViewById(R.id.line_step_two);
        lineStepThree = findViewById(R.id.line_step_three);
        itemsContainer = (LinearLayout) findViewById(R.id.items_container);
        payButton = (Button) findViewById(R.id.pay_button);
        confirmReceiveButton = (Button) findViewById(R.id.button_confirm_receive);
        reviewButton = (Button) findViewById(R.id.button_review);
        orderDao = new OrderDao(this);
        userDao = new UserDao(this);

        bindOrderData();

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payOrder();
            }
        });

        confirmReceiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelivery();
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindOrderFromDatabase(orderId, null);
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
        }
        if (orderTotal < 0) {
            orderTotal = orderItems.size() * 18.0;
        }

        bindOrderFromDatabase(orderId, orderTime);
    }

    private void bindOrderFromDatabase(String orderId, String fallbackTime) {
        OrderDao.OrderDetail detail = orderDao.getOrderDetail(orderId);
        if (detail != null) {
            orderIdView.setText(getString(R.string.order_id_format, detail.orderId));
            String orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date(detail.createdAt));
            orderTimeView.setText(getString(R.string.order_time_format, orderTime));
            orderTotalView.setText(getString(R.string.order_total_format, detail.totalAmount));
            orderTotal = detail.payAmount;
            if (detail.couponName == null || detail.couponName.trim().isEmpty()) {
                orderCouponView.setText(getString(R.string.order_coupon_none));
            } else {
                orderCouponView.setText(getString(R.string.order_coupon_format, detail.couponName,
                        detail.couponDiscount));
            }
            orderPayAmountView.setText(getString(R.string.order_pay_amount_format, detail.payAmount));
            orderAddressView.setText(getString(R.string.order_address_format,
                    safeText(detail.addressDetail)));
            orderContactView.setText(getString(R.string.order_contact_format,
                    safeText(detail.contactName), maskPhone(detail.contactPhone)));
            renderStatus(detail.status);
            bindOrderItems(orderDao.getOrderItems(orderId));
            return;
        }
        String orderTime = fallbackTime == null ? "" : fallbackTime;
        orderIdView.setText(getString(R.string.order_id_format, orderId));
        orderTimeView.setText(getString(R.string.order_time_format, orderTime));
        orderTotalView.setText(String.format(Locale.getDefault(), "总价：¥%.2f", orderTotal));
        bindOrderItemsFromExtras();
        renderStatus(OrderDao.STATUS_PENDING_PAY);
    }

    private void bindOrderItems(List<OrderDao.OrderItemDetail> items) {
        itemsContainer.removeAllViews();
        if (items == null || items.isEmpty()) {
            bindOrderItemsFromExtras();
            return;
        }
        for (OrderDao.OrderItemDetail item : items) {
            String label = item.productName + " x" + item.quantity;
            itemsContainer.addView(createItemView(label));
        }
    }

    private void bindOrderItemsFromExtras() {
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

    private void payOrder() {
        if (orderDao.getOrderStatus(orderId) != OrderDao.STATUS_PENDING_PAY) {
            return;
        }
        orderDao.updateOrderStatus(orderId, OrderDao.STATUS_PAID);
        scheduleStatusFlow();
        renderStatus(OrderDao.STATUS_PAID);
    }

    private void scheduleStatusFlow() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusIfMatch(OrderDao.STATUS_PAID, OrderDao.STATUS_PACKING);
            }
        }, 8000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusIfMatch(OrderDao.STATUS_PACKING, OrderDao.STATUS_DELIVERING);
            }
        }, 16000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusIfMatch(OrderDao.STATUS_DELIVERING, OrderDao.STATUS_DELIVERED);
            }
        }, 24000);
    }

    private void updateStatusIfMatch(int expected, int next) {
        int current = orderDao.getOrderStatus(orderId);
        if (current == expected) {
            orderDao.updateOrderStatus(orderId, next);
            renderStatus(next);
            if (next == OrderDao.STATUS_DELIVERED) {
                grantPoints();
            }
        }
    }

    private void confirmDelivery() {
        int current = orderDao.getOrderStatus(orderId);
        if (current == OrderDao.STATUS_DELIVERED) {
            return;
        }
        orderDao.updateOrderStatus(orderId, OrderDao.STATUS_DELIVERED);
        renderStatus(OrderDao.STATUS_DELIVERED);
        grantPoints();
    }

    private void grantPoints() {
        int points = (int) Math.floor(orderTotal);
        userDao.addPoints(userId, points);
        reviewButton.setEnabled(true);
    }

    private void renderStatus(int status) {
        orderStatusView.setText(getStatusLabel(status));
        highlightStep(status);
        payButton.setVisibility(status == OrderDao.STATUS_PENDING_PAY ? View.VISIBLE : View.GONE);
        confirmReceiveButton.setVisibility(status == OrderDao.STATUS_DELIVERING ? View.VISIBLE : View.GONE);
        reviewButton.setEnabled(status == OrderDao.STATUS_DELIVERED);
    }

    private void highlightStep(int status) {
        int activeColor = getResources().getColor(R.color.primary_color);
        int inactiveColor = getResources().getColor(R.color.secondary_text);
        int lineActive = getResources().getColor(R.color.primary_color);
        int lineInactive = getResources().getColor(R.color.divider_color);

        stepPendingView.setTextColor(activeColor);
        stepPackingView.setTextColor(status >= OrderDao.STATUS_PACKING ? activeColor : inactiveColor);
        stepDeliveringView.setTextColor(status >= OrderDao.STATUS_DELIVERING ? activeColor : inactiveColor);
        stepDeliveredView.setTextColor(status >= OrderDao.STATUS_DELIVERED ? activeColor : inactiveColor);

        lineStepOne.setBackgroundColor(status >= OrderDao.STATUS_PACKING ? lineActive : lineInactive);
        lineStepTwo.setBackgroundColor(status >= OrderDao.STATUS_DELIVERING ? lineActive : lineInactive);
        lineStepThree.setBackgroundColor(status >= OrderDao.STATUS_DELIVERED ? lineActive : lineInactive);
    }

    private String getStatusLabel(int status) {
        switch (status) {
            case OrderDao.STATUS_PENDING_PAY:
                return getString(R.string.order_status_pending);
            case OrderDao.STATUS_PAID:
                return getString(R.string.order_status_paid);
            case OrderDao.STATUS_PACKING:
                return getString(R.string.order_status_packing);
            case OrderDao.STATUS_DELIVERING:
                return getString(R.string.order_status_delivering);
            case OrderDao.STATUS_DELIVERED:
                return getString(R.string.order_status_delivered);
            case OrderDao.STATUS_CANCELLED:
                return getString(R.string.order_status_cancelled);
            default:
                return getString(R.string.order_status_pending);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return getString(R.string.address_phone_placeholder);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private void openReview() {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putStringArrayListExtra(ReviewActivity.EXTRA_ITEMS, buildReviewItems());
        startActivity(intent);
    }

    private ArrayList<String> buildReviewItems() {
        ArrayList<String> reviewItems = new ArrayList<String>();
        List<OrderDao.OrderItemDetail> items = orderDao.getOrderItems(orderId);
        if (items != null && !items.isEmpty()) {
            for (OrderDao.OrderItemDetail item : items) {
                if (item != null && item.productName != null && !reviewItems.contains(item.productName)) {
                    reviewItems.add(item.productName);
                }
            }
            return reviewItems;
        }
        if (orderItems != null) {
            for (String item : orderItems) {
                String[] parts = item.split(" x");
                String name = parts[0].trim();
                if (!reviewItems.contains(name)) {
                    reviewItems.add(name);
                }
            }
        }
        return reviewItems;
    }
}
