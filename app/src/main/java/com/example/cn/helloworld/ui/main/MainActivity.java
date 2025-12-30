package com.example.cn.helloworld.ui.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.support.v4.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.data.ShopLocalRepository;
import com.example.cn.helloworld.db.CategoryDao;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.model.Shop;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.entry.CouponCenterActivity;
import com.example.cn.helloworld.ui.entry.FlashSaleActivity;
import com.example.cn.helloworld.ui.entry.MemberCenterActivity;
import com.example.cn.helloworld.ui.entry.PointsCenterActivity;
import com.example.cn.helloworld.ui.entry.SearchResultActivity;
import com.example.cn.helloworld.ui.entry.ServiceHelpActivity;
import com.example.cn.helloworld.ui.food.FoodDetailActivity;
import com.example.cn.helloworld.ui.market.FoodMarketActivity;
import com.example.cn.helloworld.ui.shop.ShopDetailActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;
import com.example.cn.helloworld.ui.shop.FoodAdapter;
import com.example.cn.helloworld.utils.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseActivity {
    private final FoodLocalRepository repository = new FoodLocalRepository();
    private final ShopLocalRepository shopRepository = new ShopLocalRepository();
    private final List<String> homeCategories = new ArrayList<String>();
    private final List<Food> homeFoods = new ArrayList<Food>();
    private final List<Food> visibleHomeFoods = new ArrayList<Food>();
    private final List<Shop> recommendedShops = new ArrayList<Shop>();
    private HomeFilterAdapter homeFilterAdapter;
    private FoodAdapter homeFoodAdapter;
    private RecommendedShopAdapter recommendedShopAdapter;
    private CategoryDao categoryDao;
    private String selectedHomeCategory;
    private ImageView flashSaleImage;
    private TextView flashSaleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupHomeFilters();
        setupHomeFoods();
        setupFlashSaleCard();
        setupRecommendedShops();
        setupTodayHeroCard();

        TextView todayLabel = (TextView) findViewById(R.id.text_today_label);
        todayLabel.setText(R.string.home_today_label);

        Button browseButton = (Button) findViewById(R.id.button_browse_shops);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShopListActivity.class);
                startActivity(intent);
            }
        });

        View cartButton = findViewById(R.id.button_open_cart);
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShopListActivity.class);
                startActivity(intent);
            }
        });
        setupCartButtonIcon();

        View cartSummaryButton = findViewById(R.id.button_cart_summary);
        cartSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        View flashSaleEntry = findViewById(R.id.entry_flash_sale);
        flashSaleEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FlashSaleActivity.class));
            }
        });

        View flashSaleCard = findViewById(R.id.card_flash_sale);
        if (flashSaleCard != null) {
            flashSaleCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, FlashSaleActivity.class));
                }
            });
        }

        View recommendMore = findViewById(R.id.text_recommend_more);
        if (recommendMore != null) {
            recommendMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, ShopListActivity.class));
                }
            });
        }

        View memberEntry = findViewById(R.id.entry_member_center);
        memberEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PointsCenterActivity.class));
            }
        });

        View couponEntry = findViewById(R.id.entry_coupon_center);
        couponEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CouponCenterActivity.class));
            }
        });

        View serviceEntry = findViewById(R.id.entry_service_help);
        serviceEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ServiceHelpActivity.class));
            }
        });

        final TextView headerTitle = (TextView) findViewById(R.id.text_main_title);
        final TextView headerGreeting = (TextView) findViewById(R.id.text_main_greeting);
        final EditText searchInput = (EditText) findViewById(R.id.text_main_search);
        final String expandedSearchHint = pickSearchHint();

        updateGreeting(headerTitle, headerGreeting);
        searchInput.setHint(expandedSearchHint);
        setupFloatingHeaderBehavior(headerTitle, headerGreeting, searchInput, expandedSearchHint);
        setupSearchChips();

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String keyword = v.getText().toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    return false;
                }
                Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                intent.putExtra(SearchResultActivity.EXTRA_QUERY, keyword);
                startActivity(intent);
                return true;
            }
        });
        searchInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchWithKeyword(searchInput.getText().toString().trim());
            }
        });

        final View smartDock = findViewById(R.id.smart_dock);
        final View floatingBall = findViewById(R.id.floating_ball);
        View dockHandle = findViewById(R.id.smart_dock_handle);

        dockHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDock(smartDock, floatingBall);
            }
        });

        floatingBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDock(smartDock, floatingBall);
            }
        });

        setupFloatingBallDrag(floatingBall);

        findViewById(R.id.dock_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDock(smartDock, floatingBall);
            }
        });

        findViewById(R.id.dock_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShopListActivity.class));
            }
        });

        findViewById(R.id.dock_market).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FoodMarketActivity.class));
            }
        });

        findViewById(R.id.dock_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            }
        });

        findViewById(R.id.dock_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MemberCenterActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView headerTitle = (TextView) findViewById(R.id.text_main_title);
        TextView headerGreeting = (TextView) findViewById(R.id.text_main_greeting);
        updateGreeting(headerTitle, headerGreeting);
        loadHomeFilters();
        loadRecommendedShops();
    }

    private void setupFloatingHeaderBehavior(final TextView headerTitle,
                                             final TextView headerGreeting,
                                             final EditText searchInput,
                                             final String expandedSearchHint) {
        final View floatingHeader = findViewById(R.id.floating_header);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.main_scroll);
        if (floatingHeader == null || scrollView == null) {
            return;
        }
        final int expandedPaddingHorizontal =
                getResources().getDimensionPixelSize(R.dimen.floating_header_padding_horizontal);
        final int expandedPaddingTop =
                getResources().getDimensionPixelSize(R.dimen.floating_header_padding_top_expanded);
        final int expandedPaddingBottom =
                getResources().getDimensionPixelSize(R.dimen.floating_header_padding_bottom_expanded);
        final int compactPaddingTop =
                getResources().getDimensionPixelSize(R.dimen.floating_header_padding_top_compact);
        final int compactPaddingBottom =
                getResources().getDimensionPixelSize(R.dimen.floating_header_padding_bottom_compact);
        final int expandedSearchHeight =
                getResources().getDimensionPixelSize(R.dimen.floating_header_search_height_expanded);
        final int compactSearchHeight =
                getResources().getDimensionPixelSize(R.dimen.floating_header_search_height_compact);
        final int collapseThreshold =
                getResources().getDimensionPixelSize(R.dimen.floating_header_collapse_threshold);

        applyFloatingHeaderState(false, floatingHeader, headerTitle, headerGreeting, searchInput,
                expandedPaddingHorizontal, expandedPaddingTop, expandedPaddingBottom,
                expandedSearchHeight, expandedSearchHint);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            private boolean isCompact = false;

            @Override
            public void onScrollChanged() {
                int threshold = scrollView.getHeight() > 0 ? scrollView.getHeight() : collapseThreshold;
                boolean shouldCompact = scrollView.getScrollY() >= threshold;
                if (shouldCompact == isCompact) {
                    return;
                }
                isCompact = shouldCompact;
                if (shouldCompact) {
                    applyFloatingHeaderState(true, floatingHeader, headerTitle, headerGreeting,
                            searchInput, expandedPaddingHorizontal, compactPaddingTop, compactPaddingBottom,
                            compactSearchHeight, getString(R.string.home_search_hint_compact));
                } else {
                    applyFloatingHeaderState(false, floatingHeader, headerTitle, headerGreeting,
                            searchInput, expandedPaddingHorizontal, expandedPaddingTop, expandedPaddingBottom,
                            expandedSearchHeight, expandedSearchHint);
                }
            }
        });
    }

    private void applyFloatingHeaderState(boolean compact,
                                          View floatingHeader,
                                          TextView headerTitle,
                                          TextView headerGreeting,
                                          EditText searchInput,
                                          int paddingHorizontal,
                                          int paddingTop,
                                          int paddingBottom,
                                          int searchHeight,
                                          String hint) {
        int titleVisibility = compact ? View.GONE : View.VISIBLE;
        headerTitle.setVisibility(titleVisibility);
        headerGreeting.setVisibility(titleVisibility);
        floatingHeader.setBackgroundResource(compact
                ? R.drawable.bg_floating_header_compact
                : R.drawable.bg_floating_header);
        floatingHeader.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom);
        ViewGroup.LayoutParams params = searchInput.getLayoutParams();
        if (params != null && params.height != searchHeight) {
            params.height = searchHeight;
            searchInput.setLayoutParams(params);
        }
        searchInput.setHint(hint);
    }

    private void updateGreeting(TextView headerTitle, TextView headerGreeting) {
        if (headerTitle == null || headerGreeting == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) {
            applyGreetingTitle(headerTitle, R.string.home_greeting_morning_title,
                    R.drawable.ic_greeting_morning);
            headerGreeting.setText(R.string.home_greeting_morning_subtitle);
        } else if (hour >= 11 && hour < 17) {
            applyGreetingTitle(headerTitle, R.string.home_greeting_noon_title,
                    R.drawable.ic_greeting_noon);
            headerGreeting.setText(R.string.home_greeting_noon_subtitle);
        } else {
            applyGreetingTitle(headerTitle, R.string.home_greeting_evening_title,
                    R.drawable.ic_greeting_evening);
            headerGreeting.setText(R.string.home_greeting_evening_subtitle);
        }
    }

    private void applyGreetingTitle(TextView headerTitle, int titleResId, int iconResId) {
        headerTitle.setText(titleResId);
        applyCompoundIcon(headerTitle, iconResId);
    }

    private void setupCartButtonIcon() {
        TextView cartButton = (TextView) findViewById(R.id.button_open_cart);
        if (cartButton == null) {
            return;
        }
        applyCompoundIcon(cartButton, R.drawable.ic_delivery);
    }

    private void applyCompoundIcon(TextView target, int iconResId) {
        if (target == null) {
            return;
        }
        target.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        int tintColor = ContextCompat.getColor(this, R.color.primary_text);
        Drawable[] drawables = target.getCompoundDrawables();
        if (drawables.length > 0 && drawables[0] != null) {
            Drawable drawable = drawables[0].mutate();
            DrawableCompat.setTintList(drawable, ColorStateList.valueOf(tintColor));
        }
    }

    private String pickSearchHint() {
        String[] hints = getResources().getStringArray(R.array.home_search_hints);
        if (hints == null || hints.length == 0) {
            return getString(R.string.home_search_hint);
        }
        int index = (int) (System.currentTimeMillis() % hints.length);
        if (index < 0) {
            index = 0;
        }
        return hints[index];
    }

    private void setupSearchChips() {
        View[] chips = new View[] {
                findViewById(R.id.chip_hot_beef),
                findViewById(R.id.chip_light_meal),
                findViewById(R.id.chip_fast_delivery),
                findViewById(R.id.chip_member_offer)
        };
        for (View chip : chips) {
            if (chip == null) {
                continue;
            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    String keyword = tag == null ? "" : tag.toString();
                    openSearchWithKeyword(keyword);
                }
            });
        }
    }

    private void openSearchWithKeyword(String keyword) {
        Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
        intent.putExtra(SearchResultActivity.EXTRA_QUERY, keyword);
        startActivity(intent);
    }

    private void setupHomeFilters() {
        android.support.v7.widget.RecyclerView filtersRecycler =
                (android.support.v7.widget.RecyclerView) findViewById(R.id.recycler_home_filters);
        if (filtersRecycler == null) {
            return;
        }
        filtersRecycler.setLayoutManager(
                new android.support.v7.widget.LinearLayoutManager(
                        this,
                        android.support.v7.widget.LinearLayoutManager.HORIZONTAL,
                        false));
        homeFilterAdapter = new HomeFilterAdapter(homeCategories, new HomeFilterAdapter.OnFilterClickListener() {
            @Override
            public void onFilterClick(String category) {
                selectedHomeCategory = category;
                applyHomeCategoryFilter();
            }
        });
        filtersRecycler.setAdapter(homeFilterAdapter);
        categoryDao = new CategoryDao(this);
        loadHomeFilters();
    }

    private void setupHomeFoods() {
        android.support.v7.widget.RecyclerView foodsRecycler =
                (android.support.v7.widget.RecyclerView) findViewById(R.id.recycler_home_foods);
        if (foodsRecycler == null) {
            return;
        }
        foodsRecycler.setLayoutManager(new android.support.v7.widget.LinearLayoutManager(this));
        homeFoodAdapter = new FoodAdapter(visibleHomeFoods, new FoodAdapter.OnFoodClickListener() {
            @Override
            public void onFoodClick(Food food) {
                Intent intent = new Intent(MainActivity.this, FoodDetailActivity.class);
                intent.putExtra(FoodDetailActivity.EXTRA_FOOD_ID, food.getId());
                intent.putExtra(FoodDetailActivity.EXTRA_FOOD_NAME, food.getName());
                intent.putExtra(FoodDetailActivity.EXTRA_FOOD_PRICE, food.getPrice());
                intent.putExtra(FoodDetailActivity.EXTRA_FOOD_DESCRIPTION, food.getDescription());
                intent.putExtra(FoodDetailActivity.EXTRA_FOOD_IMAGE_URL, food.getImageUrl());
                intent.putExtra(FoodDetailActivity.EXTRA_SHOP_ID, food.getShopId());
                startActivity(intent);
            }
        });
        foodsRecycler.setAdapter(homeFoodAdapter);
    }

    private void loadHomeFilters() {
        if (homeFilterAdapter == null || categoryDao == null) {
            return;
        }
        try {
            List<Food> foods = repository.getFoods(this);
            homeFoods.clear();
            homeFoods.addAll(foods);
            Set<String> categorySet = new LinkedHashSet<String>();
            categorySet.addAll(categoryDao.getCategoryNames());
            for (Food food : foods) {
                categorySet.add(resolveCategory(food));
            }
            homeCategories.clear();
            for (String category : categorySet) {
                if (!TextUtils.isEmpty(category)) {
                    homeCategories.add(category);
                }
            }
            if (homeCategories.isEmpty()) {
                homeCategories.add(getString(R.string.category_unassigned));
            }
            if (TextUtils.isEmpty(selectedHomeCategory) || !homeCategories.contains(selectedHomeCategory)) {
                selectedHomeCategory = homeCategories.get(0);
            }
            homeFilterAdapter.setSelectedCategory(selectedHomeCategory);
            homeFilterAdapter.notifyDataSetChanged();
            applyHomeCategoryFilter();
            updateFlashSaleContent();
        } catch (java.io.IOException ignored) {
        }
    }

    private void applyHomeCategoryFilter() {
        if (homeFoodAdapter == null) {
            return;
        }
        visibleHomeFoods.clear();
        if (TextUtils.isEmpty(selectedHomeCategory)) {
            selectedHomeCategory = getString(R.string.category_unassigned);
        }
        for (Food food : homeFoods) {
            String foodCategory = resolveCategory(food);
            if (selectedHomeCategory.equals(foodCategory)) {
                visibleHomeFoods.add(food);
            }
        }
        homeFoodAdapter.notifyDataSetChanged();
    }

    private String resolveCategory(Food food) {
        if (food == null || TextUtils.isEmpty(food.getCategory())) {
            return getString(R.string.category_unassigned);
        }
        return food.getCategory();
    }

    private void setupFlashSaleCard() {
        flashSaleImage = (ImageView) findViewById(R.id.image_flash_sale);
        flashSaleName = (TextView) findViewById(R.id.text_flash_sale_name);
    }

    private void setupTodayHeroCard() {
        final View todayHeroCard = findViewById(R.id.card_today_hero);
        if (todayHeroCard == null) {
            return;
        }
        float offset = getResources().getDisplayMetrics().density * 6f;
        todayHeroCard.setAlpha(0f);
        todayHeroCard.setTranslationY(offset);
        todayHeroCard.post(new Runnable() {
            @Override
            public void run() {
                todayHeroCard.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(250)
                        .start();
            }
        });
        todayHeroCard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.animate().scaleX(0.98f).scaleY(0.98f).setDuration(120).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                }
                return false;
            }
        });
    }

    private void updateFlashSaleContent() {
        if (flashSaleName == null || flashSaleImage == null || homeFoods.isEmpty()) {
            return;
        }
        Food featured = homeFoods.get(0);
        flashSaleName.setText(featured.getName());
        ImageLoader.load(this, flashSaleImage, featured.getImageUrl());
    }

    private void setupRecommendedShops() {
        android.support.v7.widget.RecyclerView recommendedRecycler =
                (android.support.v7.widget.RecyclerView) findViewById(R.id.recycler_recommended_shops);
        if (recommendedRecycler == null) {
            return;
        }
        recommendedRecycler.setLayoutManager(new android.support.v7.widget.LinearLayoutManager(this));
        recommendedShopAdapter = new RecommendedShopAdapter(recommendedShops,
                new RecommendedShopAdapter.OnShopClickListener() {
                    @Override
                    public void onShopClick(Shop shop) {
                        Intent intent = new Intent(MainActivity.this, ShopDetailActivity.class);
                        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ID, "shop_" + shop.getId());
                        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_NAME, shop.getName());
                        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ADDRESS, shop.getAddress());
                        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_RATING, (float) shop.getRating());
                        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_IMAGE, shop.getImageUrl());
                        startActivity(intent);
                    }
                });
        recommendedRecycler.setAdapter(recommendedShopAdapter);
    }

    private void loadRecommendedShops() {
        if (recommendedShopAdapter == null) {
            return;
        }
        try {
            List<Shop> shops = shopRepository.getShops(this);
            recommendedShops.clear();
            for (int i = 0; i < shops.size() && i < 1; i++) {
                recommendedShops.add(shops.get(i));
            }
            recommendedShopAdapter.notifyDataSetChanged();
        } catch (java.io.IOException ignored) {
        }
    }

    private void showDock(final View smartDock, final View floatingBall) {
        if (smartDock.getVisibility() == View.VISIBLE) {
            return;
        }
        smartDock.setVisibility(View.VISIBLE);
        smartDock.post(new Runnable() {
            @Override
            public void run() {
                smartDock.setAlpha(0f);
                smartDock.setTranslationY(smartDock.getHeight());
                smartDock.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(220)
                        .start();
            }
        });

        floatingBall.animate()
                .alpha(0f)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(180)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        floatingBall.setVisibility(View.GONE);
                        floatingBall.setAlpha(1f);
                        floatingBall.setScaleX(1f);
                        floatingBall.setScaleY(1f);
                    }
                })
                .start();
    }

    private void hideDock(final View smartDock, final View floatingBall) {
        if (smartDock.getVisibility() != View.VISIBLE) {
            return;
        }
        smartDock.animate()
                .alpha(0f)
                .translationY(smartDock.getHeight())
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        smartDock.setVisibility(View.GONE);
                        floatingBall.setAlpha(0f);
                        floatingBall.setVisibility(View.VISIBLE);
                        floatingBall.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(180)
                                .start();
                    }
                })
                .start();
    }

    private void setupFloatingBallDrag(final View floatingBall) {
        floatingBall.setOnTouchListener(new View.OnTouchListener() {
            private float dX;
            private float dY;
            private boolean dragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewGroup parent = (ViewGroup) v.getParent();
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        dragging = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        dragging = true;
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        float maxX = parent.getWidth() - v.getWidth();
                        float maxY = parent.getHeight() - v.getHeight();
                        v.setX(clamp(newX, 0f, maxX));
                        v.setY(clamp(newY, 0f, maxY));
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragging) {
                            return v.performClick();
                        }
                        snapFloatingBall(v, parent);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void snapFloatingBall(View floatingBall, ViewGroup parent) {
        float centerX = floatingBall.getX() + floatingBall.getWidth() / 2f;
        float targetX = centerX > parent.getWidth() / 2f
                ? parent.getWidth() - floatingBall.getWidth() - dpToPx(16f)
                : dpToPx(16f);
        float targetY = clamp(floatingBall.getY(), dpToPx(16f),
                parent.getHeight() - floatingBall.getHeight() - dpToPx(32f));
        floatingBall.animate()
                .x(targetX)
                .y(targetY)
                .setDuration(180)
                .start();
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
