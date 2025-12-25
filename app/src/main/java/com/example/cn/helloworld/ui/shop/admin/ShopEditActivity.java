package com.example.cn.helloworld.ui.shop.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.ShopLocalRepository;
import com.example.cn.helloworld.model.Shop;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.ImageLoader;

public class ShopEditActivity extends BaseActivity {
    public static final String EXTRA_SHOP_ID = "extra_shop_id";

    private final ShopLocalRepository repository = new ShopLocalRepository();

    private EditText nameInput;
    private EditText addressInput;
    private EditText ratingInput;
    private EditText phoneInput;
    private EditText descriptionInput;
    private EditText imageInput;
    private ImageView previewImage;

    private int shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_edit);
        setupBackButton();

        nameInput = (EditText) findViewById(R.id.input_shop_name);
        addressInput = (EditText) findViewById(R.id.input_shop_address);
        ratingInput = (EditText) findViewById(R.id.input_shop_rating);
        phoneInput = (EditText) findViewById(R.id.input_shop_phone);
        descriptionInput = (EditText) findViewById(R.id.input_shop_description);
        imageInput = (EditText) findViewById(R.id.input_shop_image);
        previewImage = (ImageView) findViewById(R.id.image_shop_preview);
        imageInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ImageLoader.load(ShopEditActivity.this, previewImage,
                            imageInput.getText().toString());
                }
            }
        });

        Button saveButton = (Button) findViewById(R.id.button_save_shop);

        shopId = parseShopId(getIntent().getStringExtra(EXTRA_SHOP_ID));
        if (shopId > 0) {
            loadShop(shopId);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveShop();
            }
        });
    }

    private void loadShop(int id) {
        try {
            Shop shop = repository.getShopById(this, id);
            if (shop == null) {
                return;
            }
            nameInput.setText(shop.getName());
            addressInput.setText(shop.getAddress());
            ratingInput.setText(String.valueOf(shop.getRating()));
            phoneInput.setText(shop.getPhone());
            descriptionInput.setText(shop.getDescription());
            imageInput.setText(shop.getImageUrl());
            ImageLoader.load(this, previewImage, shop.getImageUrl());
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_shop_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveShop() {
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String ratingValue = ratingInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String imageUrl = imageInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double rating = 4.5;
        if (!TextUtils.isEmpty(ratingValue)) {
            try {
                rating = Double.parseDouble(ratingValue);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_rating, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (shopId <= 0) {
            shopId = (int) (System.currentTimeMillis() % 100000);
        }

        Shop shop = new Shop(shopId, name, address, rating, description, phone, imageUrl);
        try {
            repository.updateShop(this, shop);
            Toast.makeText(this, R.string.action_saved, Toast.LENGTH_SHORT).show();
            finish();
        } catch (java.io.IOException e) {
            Toast.makeText(this, R.string.error_shop_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private int parseShopId(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return 0;
        }
        if (raw.startsWith("shop_")) {
            raw = raw.substring(5);
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
