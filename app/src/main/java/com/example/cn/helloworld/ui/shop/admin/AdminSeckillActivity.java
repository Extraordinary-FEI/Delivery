package com.example.cn.helloworld.ui.shop.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.FoodLocalRepository;
import com.example.cn.helloworld.data.model.SeckillItem;
import com.example.cn.helloworld.db.SeckillDao;
import com.example.cn.helloworld.model.Food;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminSeckillActivity extends com.example.cn.helloworld.ui.common.BaseActivity {
    private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm";
    private final List<SeckillItem> items = new ArrayList<SeckillItem>();
    private AdminSeckillAdapter adapter;
    private SeckillDao seckillDao;
    private final FoodLocalRepository foodRepository = new FoodLocalRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_seckill);
        setupBackButton();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_admin_seckill);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminSeckillAdapter(items, new AdminSeckillAdapter.OnSeckillAdminActionListener() {
            @Override
            public void onEdit(SeckillItem item) {
                showEditDialog(item);
            }

            @Override
            public void onToggle(SeckillItem item) {
                toggleStatus(item);
            }

            @Override
            public void onDelete(SeckillItem item) {
                deleteItem(item);
            }
        });
        recyclerView.setAdapter(adapter);

        seckillDao = new SeckillDao(this);
        seckillDao.seedDefaults(this);

        findViewById(R.id.button_add_seckill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(null);
            }
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        items.clear();
        items.addAll(seckillDao.getSeckillItems(this));
        adapter.notifyDataSetChanged();
    }

    private void toggleStatus(SeckillItem item) {
        if (item == null) {
            return;
        }
        int status = item.status == 1 ? 0 : 1;
        seckillDao.updateStatus(item.id, status);
        loadItems();
    }

    private void deleteItem(SeckillItem item) {
        if (item == null) {
            return;
        }
        seckillDao.delete(item.id);
        loadItems();
    }

    private void showEditDialog(final SeckillItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_seckill, null);
        final Spinner productSpinner = (Spinner) view.findViewById(R.id.spinner_seckill_product);
        final EditText priceInput = (EditText) view.findViewById(R.id.input_seckill_price);
        final EditText stockInput = (EditText) view.findViewById(R.id.input_seckill_stock);
        final EditText startInput = (EditText) view.findViewById(R.id.input_seckill_start);
        final EditText endInput = (EditText) view.findViewById(R.id.input_seckill_end);

        final List<Food> foods = loadFoods();
        final List<String> displayNames = buildFoodDisplayNames(foods);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, displayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productSpinner.setAdapter(adapter);

        long now = System.currentTimeMillis();
        String formattedNow = formatTimeInput(now);
        if (item != null) {
            priceInput.setText(String.valueOf(item.seckillPrice));
            stockInput.setText(String.valueOf(item.stock));
            startInput.setText(formatTimeInput(item.startTime));
            endInput.setText(formatTimeInput(item.endTime));
            int selection = resolveSelectionIndex(foods, item.productId);
            if (selection >= 0) {
                productSpinner.setSelection(selection);
            }
        } else {
            long defaultEnd = addHours(now, 2);
            startInput.setText(formattedNow);
            endInput.setText(formatTimeInput(defaultEnd));
        }

        new AlertDialog.Builder(this)
                .setTitle(item == null ? R.string.admin_seckill_add : R.string.admin_seckill_edit)
                .setView(view)
                .setPositiveButton(R.string.common_save, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String productId = resolveProductId(foods, productSpinner.getSelectedItemPosition());
                        String priceText = priceInput.getText().toString().trim();
                        String stockText = stockInput.getText().toString().trim();
                        String startText = startInput.getText().toString().trim();
                        String endText = endInput.getText().toString().trim();
                        if (TextUtils.isEmpty(productId) || TextUtils.isEmpty(priceText)
                                || TextUtils.isEmpty(stockText) || TextUtils.isEmpty(startText)
                                || TextUtils.isEmpty(endText)) {
                            Toast.makeText(AdminSeckillActivity.this, R.string.admin_seckill_input_error,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        long startTime = parseTimeInput(startText);
                        long endTime = parseTimeInput(endText);
                        if (startTime <= 0 || endTime <= 0 || startTime >= endTime) {
                            Toast.makeText(AdminSeckillActivity.this, R.string.admin_seckill_input_error,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SeckillItem updated = new SeckillItem(
                                item == null ? 0 : item.id,
                                productId,
                                Double.parseDouble(priceText),
                                Integer.parseInt(stockText),
                                startTime,
                                endTime,
                                item == null ? 1 : item.status,
                                resolveFoodById(foods, productId)
                        );
                        seckillDao.insertOrUpdate(updated);
                        loadItems();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private List<Food> loadFoods() {
        try {
            return foodRepository.getFoods(this);
        } catch (IOException e) {
            return new ArrayList<Food>();
        }
    }

    private List<String> buildFoodDisplayNames(List<Food> foods) {
        List<String> display = new ArrayList<String>();
        if (foods == null || foods.isEmpty()) {
            display.add(getString(R.string.admin_seckill_no_products));
            return display;
        }
        for (Food food : foods) {
            display.add(food.getName() + " (" + food.getId() + ")");
        }
        return display;
    }

    private int resolveSelectionIndex(List<Food> foods, String productId) {
        if (foods == null || TextUtils.isEmpty(productId)) {
            return -1;
        }
        for (int i = 0; i < foods.size(); i++) {
            if (productId.equals(foods.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private String resolveProductId(List<Food> foods, int position) {
        if (foods == null || foods.isEmpty() || position < 0 || position >= foods.size()) {
            return "";
        }
        return foods.get(position).getId();
    }

    private Food resolveFoodById(List<Food> foods, String productId) {
        if (foods == null || TextUtils.isEmpty(productId)) {
            return null;
        }
        for (Food food : foods) {
            if (productId.equals(food.getId())) {
                return food;
            }
        }
        return null;
    }

    private long parseTimeInput(String value) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        try {
            Date date = format.parse(value);
            if (date == null) {
                return -1;
            }
            return date.getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    private String formatTimeInput(long time) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        return format.format(new Date(time));
    }

    private long addHours(long time, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTimeInMillis();
    }
}
