package com.example.cn.helloworld.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.OrderDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends BaseActivity implements OrderListAdapter.OnOrderActionListener {
    public static final String EXTRA_FILTER = "extra_filter";
    public static final int FILTER_ALL = 0;
    public static final int FILTER_PENDING = 1;
    public static final int FILTER_ACTIVE = 2;
    public static final int FILTER_COMPLETED = 3;

    private final List<OrderDao.OrderSummary> orders = new ArrayList<OrderDao.OrderSummary>();
    private OrderListAdapter adapter;
    private OrderDao orderDao;
    private int userId;
    private int currentFilter = FILTER_ALL;
    private TextView filterAll;
    private TextView filterPending;
    private TextView filterActive;
    private TextView filterCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        setupBackButton();

        orderDao = new OrderDao(this);
        userId = parseUserId(SessionManager.getUserId(this));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderListAdapter(orders, orderDao, this);
        recyclerView.setAdapter(adapter);

        filterAll = (TextView) findViewById(R.id.filter_all);
        filterPending = (TextView) findViewById(R.id.filter_pending);
        filterActive = (TextView) findViewById(R.id.filter_active);
        filterCompleted = (TextView) findViewById(R.id.filter_completed);

        filterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter(FILTER_ALL);
            }
        });
        filterPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter(FILTER_PENDING);
            }
        });
        filterActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter(FILTER_ACTIVE);
            }
        });
        filterCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter(FILTER_COMPLETED);
            }
        });

        int filterExtra = getIntent().getIntExtra(EXTRA_FILTER, FILTER_ALL);
        applyFilter(filterExtra);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void applyFilter(int filter) {
        currentFilter = filter;
        updateFilterStyles();
        loadOrders();
    }

    private void updateFilterStyles() {
        int activeColor = getResources().getColor(R.color.primary_color);
        int inactiveColor = getResources().getColor(R.color.secondary_text);
        filterAll.setTextColor(currentFilter == FILTER_ALL ? activeColor : inactiveColor);
        filterPending.setTextColor(currentFilter == FILTER_PENDING ? activeColor : inactiveColor);
        filterActive.setTextColor(currentFilter == FILTER_ACTIVE ? activeColor : inactiveColor);
        filterCompleted.setTextColor(currentFilter == FILTER_COMPLETED ? activeColor : inactiveColor);
    }

    private void loadOrders() {
        orders.clear();
        List<OrderDao.OrderSummary> all = orderDao.getOrdersForUser(userId);
        for (OrderDao.OrderSummary order : all) {
            if (matchesFilter(order.status)) {
                orders.add(order);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean matchesFilter(int status) {
        if (currentFilter == FILTER_ALL) {
            return true;
        }
        if (currentFilter == FILTER_PENDING) {
            return status == OrderDao.STATUS_PENDING_PAY;
        }
        if (currentFilter == FILTER_ACTIVE) {
            return status == OrderDao.STATUS_PAID
                    || status == OrderDao.STATUS_PACKING
                    || status == OrderDao.STATUS_DELIVERING;
        }
        if (currentFilter == FILTER_COMPLETED) {
            return status == OrderDao.STATUS_DELIVERED;
        }
        return true;
    }

    @Override
    public void onOrderClick(OrderDao.OrderSummary order) {
        if (order == null) {
            return;
        }
        Intent intent = new Intent(this, OrderConfirmActivity.class);
        intent.putExtra(OrderConfirmActivity.EXTRA_ORDER_ID, order.orderId);
        intent.putExtra(OrderConfirmActivity.EXTRA_USER_ID, userId);
        startActivity(intent);
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

