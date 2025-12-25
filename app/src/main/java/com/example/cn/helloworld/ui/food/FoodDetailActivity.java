package com.example.cn.helloworld.ui.food;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.data.cart.FoodItem;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.db.UserContentDao;
import com.example.cn.helloworld.db.OrderDao;
import com.example.cn.helloworld.db.ReviewDao;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.order.ReviewActivity;
import com.example.cn.helloworld.ui.shop.ShopDetailActivity;
import com.example.cn.helloworld.utils.ImageLoader;
import com.example.cn.helloworld.utils.SessionManager;

import java.io.IOException;
import java.util.Locale;
import java.util.List;

public class FoodDetailActivity extends BaseActivity {
    public static final String EXTRA_FOOD_NAME = "extra_food_name";
    public static final String EXTRA_FOOD_PRICE = "extra_food_price";
    public static final String EXTRA_FOOD_DESCRIPTION = "extra_food_description";
    public static final String EXTRA_FOOD_IMAGE_RES = "extra_food_image_res";
    public static final String EXTRA_FOOD_IMAGE_URL = "extra_food_image_url";
    public static final String EXTRA_FOOD_ID = "extra_food_id";
    public static final String EXTRA_FOOD_DESC = EXTRA_FOOD_DESCRIPTION;
    public static final String EXTRA_SHOP_NAME = "extra_shop_name";
    public static final String EXTRA_SHOP_ID = "extra_shop_id";

    private TextView cartCountView;
    private TextView cartTotalView;
    private TextView favoriteButton;
    private TextView enterShopButton;
    private LinearLayout reviewList;
    private Button addReviewButton;
    private UserContentDao contentDao;
    private ReviewDao reviewDao;
    private OrderDao orderDao;
    private int userId;
    private Food currentFood;
    private String shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        setupBackButton();

        ImageView foodImage = (ImageView) findViewById(R.id.food_image);
        TextView foodName = (TextView) findViewById(R.id.food_name);
        TextView foodPrice = (TextView) findViewById(R.id.food_price);
        TextView foodDescription = (TextView) findViewById(R.id.food_description);
        Button addToCartButton = (Button) findViewById(R.id.button_add_to_cart);
        favoriteButton = (TextView) findViewById(R.id.button_favorite);
        enterShopButton = (TextView) findViewById(R.id.button_enter_shop);
        reviewList = (LinearLayout) findViewById(R.id.layout_review_list);
        addReviewButton = (Button) findViewById(R.id.button_add_review);
        cartCountView = (TextView) findViewById(R.id.cart_count);
        cartTotalView = (TextView) findViewById(R.id.cart_total);

        contentDao = new UserContentDao(this);
        reviewDao = new ReviewDao(this);
        orderDao = new OrderDao(this);
        userId = parseUserId(SessionManager.getUserId(this));

        String foodId = getIntent().getStringExtra(EXTRA_FOOD_ID);
        String name = getIntent().getStringExtra(EXTRA_FOOD_NAME);
        double price = getIntent().getDoubleExtra(EXTRA_FOOD_PRICE, 18.0);
        String description = getIntent().getStringExtra(EXTRA_FOOD_DESCRIPTION);
        int imageResId = getIntent().getIntExtra(EXTRA_FOOD_IMAGE_RES, R.mipmap.ic_launcher);
        String imageUrl = getIntent().getStringExtra(EXTRA_FOOD_IMAGE_URL);
        shopId = getIntent().getStringExtra(EXTRA_SHOP_ID);

        if (name == null) {
            name = getString(R.string.default_food_name);
        }
        if (description == null) {
            description = getString(R.string.default_food_description);
        }

        currentFood = new Food(foodId, name, shopId, description, price, imageUrl);
        final FoodItem foodItem = new FoodItem(name, price, description, imageResId, imageUrl);

        ImageLoader.load(this, foodImage, imageUrl);
        foodName.setText(foodItem.getName());
        foodPrice.setText(getString(R.string.food_price_format, formatPrice(foodItem.getPrice())));
        foodDescription.setText(foodItem.getDescription());

        contentDao.addBrowseHistory(userId, currentFood);
        updateFavoriteState();
        updateCartSummary();
        resolveShopId();
        enterShopButton.setEnabled(!TextUtils.isEmpty(shopId));
        bindReviews();

        addToCartButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                CartManager.getInstance(FoodDetailActivity.this).addItem(foodItem);
                updateCartSummary();
            }
        });

        favoriteButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                contentDao.toggleFavorite(userId, currentFood);
                updateFavoriteState();
            }
        });

        enterShopButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                openShop();
            }
        });

        addReviewButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                openReview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindReviews();
    }

    private void updateCartSummary() {
        CartManager cartManager = CartManager.getInstance(this);
        cartCountView.setText(getString(R.string.cart_count_format, cartManager.getTotalCount()));
        cartTotalView.setText(getString(R.string.cart_total_format, formatPrice(cartManager.getTotalPrice())));
    }

    private String formatPrice(double price) {
        return String.format(Locale.CHINA, "%.2f", price);
    }

    private void updateFavoriteState() {
        boolean isFavorite = contentDao.isFavorite(userId, resolveFoodId(currentFood));
        favoriteButton.setText(isFavorite ? getString(R.string.favorite_added) : getString(R.string.favorite_add));
    }

    private void bindReviews() {
        reviewList.removeAllViews();
        String reviewKey = resolveReviewKey();
        List<ReviewDao.Review> reviews = reviewDao.getReviewsForFood(reviewKey);
        if (reviews.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.review_empty);
            empty.setTextColor(getResources().getColor(R.color.secondary_text));
            reviewList.addView(empty);
        } else {
            for (ReviewDao.Review review : reviews) {
                TextView textView = new TextView(this);
                textView.setText(review.content);
                textView.setTextColor(getResources().getColor(R.color.primary_text));
                int padding = (int) (getResources().getDisplayMetrics().density * 8);
                textView.setPadding(0, padding, 0, padding);
                reviewList.addView(textView);
            }
        }
        boolean canReview = orderDao.hasPurchasedProduct(userId, reviewKey);
        addReviewButton.setEnabled(canReview);
    }

    private void openReview() {
        String reviewKey = resolveReviewKey();
        if (!orderDao.hasPurchasedProduct(userId, reviewKey)) {
            return;
        }
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(ReviewActivity.EXTRA_FOOD_ID, reviewKey);
        intent.putExtra(ReviewActivity.EXTRA_FOOD_NAME, currentFood.getName());
        startActivity(intent);
    }

    private void openShop() {
        if (TextUtils.isEmpty(shopId)) {
            return;
        }
        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ID, shopId);
        startActivity(intent);
    }

    private void resolveShopId() {
        if (!TextUtils.isEmpty(shopId)) {
            return;
        }
        if (currentFood == null || TextUtils.isEmpty(currentFood.getId())) {
            return;
        }
        FoodLocalRepository repository = new FoodLocalRepository();
        try {
            Food food = repository.getFoodById(this, currentFood.getId());
            if (food != null) {
                shopId = food.getShopId();
                currentFood = new Food(food.getId(), food.getName(), shopId,
                        food.getDescription(), food.getPrice(), food.getImageUrl());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        enterShopButton.setEnabled(!TextUtils.isEmpty(shopId));
    }

    private String resolveReviewKey() {
        String foodId = resolveFoodId(currentFood);
        if (orderDao.hasPurchasedProduct(userId, foodId)) {
            return foodId;
        }
        if (currentFood != null && !TextUtils.isEmpty(currentFood.getName())
                && orderDao.hasPurchasedProduct(userId, currentFood.getName())) {
            return currentFood.getName();
        }
        return foodId;
    }

    private String resolveFoodId(Food food) {
        if (food == null) {
            return "";
        }
        if (food.getId() != null && food.getId().trim().length() > 0) {
            return food.getId();
        }
        return food.getName();
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
