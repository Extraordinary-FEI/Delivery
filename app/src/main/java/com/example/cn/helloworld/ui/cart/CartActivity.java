package com.example.cn.helloworld.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.entry.MemberCenterActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.ui.market.FoodMarketActivity;
import com.example.cn.helloworld.ui.order.CheckoutActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity implements CartAdapter.CartActionListener {

    private RecyclerView cartRecyclerView;
    private TextView totalAmountView;
    private TextView goodsTotalView;
    private TextView payableView;
    private TextView emptyView;
    private View contentContainer;
    private TextView clearButton;
    private Button checkoutButton;
    private CartAdapter cartAdapter;
    private final List<CartItem> cartItems = new ArrayList<CartItem>();
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupBackButton();

        cartRecyclerView = (RecyclerView) findViewById(R.id.recycler_cart);
        totalAmountView = (TextView) findViewById(R.id.text_total_amount);
        goodsTotalView = (TextView) findViewById(R.id.text_fee_goods_total);
        payableView = (TextView) findViewById(R.id.text_fee_payable);
        emptyView = (TextView) findViewById(R.id.text_empty_cart);
        contentContainer = findViewById(R.id.cart_content_container);
        clearButton = (TextView) findViewById(R.id.button_clear_cart);
        checkoutButton = (Button) findViewById(R.id.button_checkout);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartManager = CartManager.getInstance(this);
        cartAdapter = new CartAdapter(cartItems, this);
        cartRecyclerView.setAdapter(cartAdapter);

        setupDockActions();
        refreshCartItems();

        if (clearButton != null) {
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartManager.clear();
                    refreshCartItems();
                }
            });
        }

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartItems();
    }

    @Override
    public void onQuantityChanged(CartItem item) {
        cartManager.updateItemQuantity(item.getName(), item.getQuantity());
        updateTotal();
    }

    @Override
    public void onItemRemoved(int position, CartItem item) {
        if (position >= 0 && position < cartItems.size()) {
            cartManager.removeItem(item.getName());
            cartItems.remove(position);
            cartAdapter.notifyItemRemoved(position);
            cartAdapter.notifyItemRangeChanged(position, cartItems.size() - position);
            updateTotal();
        }
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(lineTotal);
        }
        String totalText = getString(R.string.cart_total_format, total.toPlainString());
        totalAmountView.setText(totalText);
        if (goodsTotalView != null) {
            goodsTotalView.setText(getString(R.string.cart_amount_format, total.toPlainString()));
        }
        if (payableView != null) {
            payableView.setText(getString(R.string.cart_amount_format, total.toPlainString()));
        }
        boolean hasItems = !cartItems.isEmpty();
        checkoutButton.setEnabled(hasItems);
        if (clearButton != null) {
            clearButton.setEnabled(hasItems);
        }
        if (emptyView != null) {
            emptyView.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        }
        if (contentContainer != null) {
            contentContainer.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        }
    }

    private void refreshCartItems() {
        cartItems.clear();
        for (com.example.cn.helloworld.data.cart.CartItem item : cartManager.getItems()) {
            cartItems.add(new CartItem(
                    item.getName(),
                    BigDecimal.valueOf(item.getPrice()),
                    item.getQuantity(),
                    item.getImageUrl()));
        }
        cartAdapter.notifyDataSetChanged();
        updateTotal();
    }

    private void setupDockActions() {
        findViewById(R.id.dock_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.dock_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, ShopListActivity.class));
            }
        });

        findViewById(R.id.dock_market).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, FoodMarketActivity.class));
            }
        });

        findViewById(R.id.dock_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on cart.
            }
        });

        findViewById(R.id.dock_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, MemberCenterActivity.class));
            }
        });
    }
}
