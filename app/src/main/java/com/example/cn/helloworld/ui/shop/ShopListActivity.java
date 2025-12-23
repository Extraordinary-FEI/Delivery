package com.example.cn.helloworld.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.data.JsonUtils;
import com.example.cn.helloworld.model.Shop;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShopListActivity extends BaseActivity implements ShopAdapter.OnShopClickListener {
    private final List<Shop> shops = new ArrayList<Shop>();
    private ShopAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_shops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShopAdapter(shops, this);
        recyclerView.setAdapter(adapter);

        loadShopData();
    }

    private void loadShopData() {
        try {
            shops.clear();
            shops.addAll(JsonUtils.loadShops(this));
            adapter.notifyDataSetChanged();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_shop_load_failed, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(this, R.string.error_shop_format_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShopClick(Shop shop) {
        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ID, "shop_" + shop.getId());
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_NAME, shop.getName());
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_ADDRESS, shop.getAddress());
        intent.putExtra(ShopDetailActivity.EXTRA_SHOP_RATING, (float) shop.getRating());
        startActivity(intent);
    }
}
