package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.data.ShopLocalRepository;
import com.example.cn.helloworld.data.db.DeliveryDatabaseHelper;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.model.Shop;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.food.FoodDetailActivity;
import com.example.cn.helloworld.ui.shop.ShopDetailActivity;
import com.example.cn.helloworld.ui.entry.SettingsActivity;
import com.example.cn.helloworld.ui.entry.CouponCenterActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends BaseActivity implements SearchResultAdapter.OnResultClickListener {
    public static final String EXTRA_QUERY = "extra_query";

    private final List<SearchResultItem> results = new ArrayList<SearchResultItem>();
    private SearchResultAdapter adapter;
    private TextView emptyView;
    private EditText searchInput;
    private DeliveryDatabaseHelper dbHelper;
    private FoodLocalRepository foodRepository;
    private ShopLocalRepository shopRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        setupBackButton();

        dbHelper = new DeliveryDatabaseHelper(this);
        foodRepository = new FoodLocalRepository();
        shopRepository = new ShopLocalRepository();

        searchInput = (EditText) findViewById(R.id.input_search_keyword);
        emptyView = (TextView) findViewById(R.id.text_search_empty);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(results, this);
        recyclerView.setAdapter(adapter);

        String query = getIntent().getStringExtra(EXTRA_QUERY);
        if (query != null) {
            searchInput.setText(query);
            searchInput.setSelection(query.length());
            runSearch(query);
        }

        findViewById(R.id.button_search_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = searchInput.getText().toString().trim();
                runSearch(keyword);
            }
        });
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                String keyword = v.getText().toString().trim();
                runSearch(keyword);
                return true;
            }
        });
    }

    private void runSearch(String keyword) {
        results.clear();
        if (!TextUtils.isEmpty(keyword)) {
            try {
                foodRepository.getFoods(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            results.addAll(queryDatabase(keyword));
            results.addAll(queryServices(keyword));
        }
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<SearchResultItem> queryDatabase(String keyword) {
        List<SearchResultItem> items = new ArrayList<SearchResultItem>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String like = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(
                "SELECT id, name, 'product' AS type FROM products WHERE name LIKE ? " +
                        "UNION " +
                        "SELECT id, name, 'shop' AS type FROM shops WHERE name LIKE ?",
                new String[]{like, like}
        );
        try {
            while (cursor.moveToNext()) {
                items.add(new SearchResultItem(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                ));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    private List<SearchResultItem> queryServices(String keyword) {
        List<SearchResultItem> items = new ArrayList<SearchResultItem>();
        String[] services = new String[]{
                getString(R.string.service_help_title),
                getString(R.string.user_action_settings),
                getString(R.string.coupon_center_title)
        };
        for (String service : services) {
            if (service.contains(keyword)) {
                items.add(new SearchResultItem(service, service, "service"));
            }
        }
        return items;
    }

    @Override
    public void onResultClick(SearchResultItem item) {
        if (item == null) {
            return;
        }
        switch (item.type) {
            case "product":
                openFood(item.id);
                break;
            case "shop":
                openShop(item.id);
                break;
            default:
                openService(item.title);
                break;
        }
    }

    private void openFood(String foodId) {
        try {
            Food food = foodRepository.getFoodById(this, foodId);
            if (food == null) {
                return;
            }
            Intent intent = new Intent(this, FoodDetailActivity.class);
            intent.putExtra(FoodDetailActivity.EXTRA_FOOD_ID, food.getId());
            intent.putExtra(FoodDetailActivity.EXTRA_FOOD_NAME, food.getName());
            intent.putExtra(FoodDetailActivity.EXTRA_FOOD_PRICE, food.getPrice());
            intent.putExtra(FoodDetailActivity.EXTRA_FOOD_DESCRIPTION, food.getDescription());
            intent.putExtra(FoodDetailActivity.EXTRA_FOOD_IMAGE_URL, food.getImageUrl());
            intent.putExtra(FoodDetailActivity.EXTRA_SHOP_ID, food.getShopId());
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openShop(String shopId) {
        try {
            int parsedId = parseShopId(shopId);
            Shop shop = shopRepository.getShopById(this, parsedId);
            Intent intent = new Intent(this, ShopDetailActivity.class);
            if (shop != null) {
                intent.putExtra(ShopDetailActivity.EXTRA_SHOP, shop);
            } else {
                intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ID, shopId);
            }
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openService(String title) {
        if (getString(R.string.user_action_settings).equals(title)) {
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }
        if (getString(R.string.coupon_center_title).equals(title)) {
            startActivity(new Intent(this, CouponCenterActivity.class));
            return;
        }
        startActivity(new Intent(this, ServiceHelpActivity.class));
    }

    private int parseShopId(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return 0;
        }
        if (raw.startsWith("shop_")) {
            try {
                return Integer.parseInt(raw.substring(5));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static class SearchResultItem {
        public final String id;
        public final String title;
        public final String type;

        public SearchResultItem(String id, String title, String type) {
            this.id = id;
            this.title = title;
            this.type = type;
        }
    }
}
