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
import com.example.cn.helloworld.ui.order.CheckoutActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity implements CartAdapter.CartActionListener {

    private RecyclerView cartRecyclerView;
    private TextView totalAmountView;
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
        checkoutButton = (Button) findViewById(R.id.button_checkout);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartManager = CartManager.getInstance(this);
        cartAdapter = new CartAdapter(cartItems, this);
        cartRecyclerView.setAdapter(cartAdapter);

        refreshCartItems();

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
        totalAmountView.setText(getString(R.string.cart_total_format, total.toPlainString()));
        checkoutButton.setEnabled(!cartItems.isEmpty());
    }

    private void refreshCartItems() {
        cartItems.clear();
        for (com.example.cn.helloworld.data.cart.CartItem item : cartManager.getItems()) {
            cartItems.add(new CartItem(item.getName(), BigDecimal.valueOf(item.getPrice()), item.getQuantity()));
        }
        cartAdapter.notifyDataSetChanged();
        updateTotal();
    }
}
