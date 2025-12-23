package com.example.cn.helloworld.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.order.CheckoutActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartActionListener {

    private RecyclerView cartRecyclerView;
    private TextView totalAmountView;
    private Button checkoutButton;
    private CartAdapter cartAdapter;
    private final List<CartItem> cartItems = new ArrayList<CartItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = (RecyclerView) findViewById(R.id.recycler_cart);
        totalAmountView = (TextView) findViewById(R.id.text_total_amount);
        checkoutButton = (Button) findViewById(R.id.button_checkout);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        seedCartItems();

        cartAdapter = new CartAdapter(cartItems, this);
        cartRecyclerView.setAdapter(cartAdapter);

        updateTotal();

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        updateTotal();
    }

    @Override
    public void onItemRemoved(int position) {
        if (position >= 0 && position < cartItems.size()) {
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

    private void seedCartItems() {
        cartItems.add(new CartItem("定制应援手幅", new BigDecimal("39.9"), 1));
        cartItems.add(new CartItem("纪念徽章套装", new BigDecimal("59.0"), 2));
        cartItems.add(new CartItem("专辑海报", new BigDecimal("19.5"), 1));
    }
}
