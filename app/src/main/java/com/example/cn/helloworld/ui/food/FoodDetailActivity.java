package com.example.cn.helloworld.ui.food;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.data.cart.FoodItem;

import java.util.Locale;

public class FoodDetailActivity extends BaseActivity {
    public static final String EXTRA_FOOD_NAME = "extra_food_name";
    public static final String EXTRA_FOOD_PRICE = "extra_food_price";
    public static final String EXTRA_FOOD_DESCRIPTION = "extra_food_description";
    public static final String EXTRA_FOOD_IMAGE_RES = "extra_food_image_res";
    public static final String EXTRA_FOOD_DESC = EXTRA_FOOD_DESCRIPTION;
    public static final String EXTRA_SHOP_NAME = "extra_shop_name";

    private TextView cartCountView;
    private TextView cartTotalView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        ImageView foodImage = (ImageView) findViewById(R.id.food_image);
        TextView foodName = (TextView) findViewById(R.id.food_name);
        TextView foodPrice = (TextView) findViewById(R.id.food_price);
        TextView foodDescription = (TextView) findViewById(R.id.food_description);
        Button addToCartButton = (Button) findViewById(R.id.button_add_to_cart);
        cartCountView = (TextView) findViewById(R.id.cart_count);
        cartTotalView = (TextView) findViewById(R.id.cart_total);

        String name = getIntent().getStringExtra(EXTRA_FOOD_NAME);
        double price = getIntent().getDoubleExtra(EXTRA_FOOD_PRICE, 18.0);
        String description = getIntent().getStringExtra(EXTRA_FOOD_DESCRIPTION);
        int imageResId = getIntent().getIntExtra(EXTRA_FOOD_IMAGE_RES, R.mipmap.ic_launcher);

        if (name == null) {
            name = getString(R.string.default_food_name);
        }
        if (description == null) {
            description = getString(R.string.default_food_description);
        }

        final FoodItem foodItem = new FoodItem(name, price, description, imageResId);

        foodImage.setImageResource(foodItem.getImageResId());
        foodName.setText(foodItem.getName());
        foodPrice.setText(getString(R.string.food_price_format, formatPrice(foodItem.getPrice())));
        foodDescription.setText(foodItem.getDescription());

        updateCartSummary();

        addToCartButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                CartManager.getInstance(FoodDetailActivity.this).addItem(foodItem);
                updateCartSummary();
            }
        });
    }

    private void updateCartSummary() {
        CartManager cartManager = CartManager.getInstance(this);
        cartCountView.setText(getString(R.string.cart_count_format, cartManager.getTotalCount()));
        cartTotalView.setText(getString(R.string.cart_total_format, formatPrice(cartManager.getTotalPrice())));
    }

    private String formatPrice(double price) {
        return String.format(Locale.CHINA, "%.2f", price);
    }
}
