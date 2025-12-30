package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.data.cart.FoodItem;
import com.example.cn.helloworld.data.model.SeckillItem;
import com.example.cn.helloworld.db.SeckillDao;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.shop.admin.AdminSeckillActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlashSaleActivity extends BaseActivity {
    private final List<SeckillItem> items = new ArrayList<SeckillItem>();
    private FlashSaleAdapter adapter;
    private SeckillDao seckillDao;
    private TextView countdownView;
    private TextView statusView;
    private CountDownTimer countDownTimer;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_sale);
        setupBackButton();

        countdownView = (TextView) findViewById(R.id.text_flash_sale_countdown);
        statusView = (TextView) findViewById(R.id.text_flash_sale_status);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_flash_sale);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FlashSaleAdapter(items, new FlashSaleAdapter.OnSeckillActionListener() {
            @Override
            public void onSeckillClick(SeckillItem item) {
            }

            @Override
            public void onSeckillBuy(SeckillItem item) {
                handleSeckillBuy(item);
            }
        });
        recyclerView.setAdapter(adapter);

        seckillDao = new SeckillDao(this);

        TextView adminHint = (TextView) findViewById(R.id.text_flash_sale_admin);
        if (SessionManager.isAdmin(this)) {
            adminHint.setText(R.string.flash_sale_admin_hint);
        } else {
            adminHint.setText(R.string.flash_sale_user_hint);
        }

        Button manageButton = (Button) findViewById(R.id.button_manage_seckill);
        if (SessionManager.isAdmin(this)) {
            manageButton.setVisibility(View.VISIBLE);
            manageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FlashSaleActivity.this, AdminSeckillActivity.class));
                }
            });
        } else {
            manageButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSeckillList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCountdown();
    }

    private void loadSeckillList() {
        long now = System.currentTimeMillis();
        List<SeckillItem> activeItems = seckillDao.getActiveSeckillItems(this, now);
        items.clear();
        items.addAll(activeItems);
        adapter.notifyDataSetChanged();

        if (!activeItems.isEmpty()) {
            statusView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            long minEnd = activeItems.get(0).endTime;
            for (SeckillItem item : activeItems) {
                if (item.endTime < minEnd) {
                    minEnd = item.endTime;
                }
            }
            startCountdown(minEnd);
            return;
        }

        stopCountdown();
        recyclerView.setVisibility(View.GONE);

        List<SeckillItem> upcomingItems = seckillDao.getUpcomingSeckillItems(this, now);
        if (!upcomingItems.isEmpty()) {
            String time = formatTime(upcomingItems.get(0).startTime);
            statusView.setText(getString(R.string.flash_sale_status_upcoming_format, time));
        } else {
            List<SeckillItem> endedItems = seckillDao.getEndedSeckillItems(this, now);
            if (!endedItems.isEmpty()) {
                statusView.setText(R.string.flash_sale_status_ended);
            } else {
                statusView.setText(R.string.flash_sale_status_empty);
            }
        }
        statusView.setVisibility(View.VISIBLE);
        countdownView.setText(R.string.flash_sale_not_active);
    }

    private void handleSeckillBuy(SeckillItem item) {
        if (item == null) {
            return;
        }
        Food food = item.food;
        String name = food == null ? item.productId : food.getName();
        String desc = food == null ? "" : food.getDescription();
        String imageUrl = food == null ? null : food.getImageUrl();
        CartManager cartManager = CartManager.getInstance(this);
        if (cartManager.getItemQuantity(name) >= 2) {
            Toast.makeText(this, R.string.flash_sale_limit_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        if (item.stock <= 0) {
            Toast.makeText(this, R.string.flash_sale_stock_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean decreased = seckillDao.decreaseStock(item.id);
        if (!decreased) {
            Toast.makeText(this, R.string.flash_sale_stock_empty, Toast.LENGTH_SHORT).show();
            loadSeckillList();
            return;
        }

        FoodItem foodItem = new FoodItem(name, item.seckillPrice, desc, 0, imageUrl);
        cartManager.addItem(foodItem);
        Toast.makeText(this, R.string.flash_sale_added_cart, Toast.LENGTH_SHORT).show();
        loadSeckillList();
    }

    private void startCountdown(long endTime) {
        stopCountdown();
        long now = System.currentTimeMillis();
        long remaining = endTime - now;
        if (remaining <= 0) {
            countdownView.setText(R.string.flash_sale_ended);
            return;
        }
        countDownTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownView.setText(getString(R.string.flash_sale_countdown_format,
                        formatDuration(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                countdownView.setText(R.string.flash_sale_ended);
                loadSeckillList();
            }
        };
        countDownTimer.start();
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private String formatDuration(long millis) {
        long totalSeconds = Math.max(0, millis / 1000);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        return format.format(time);
    }
}
