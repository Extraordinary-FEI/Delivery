package com.example.cn.helloworld.ui.shop.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.model.SeckillItem;
import com.example.cn.helloworld.db.SeckillDao;

import java.util.ArrayList;
import java.util.List;

public class AdminSeckillActivity extends com.example.cn.helloworld.ui.common.BaseActivity {
    private final List<SeckillItem> items = new ArrayList<SeckillItem>();
    private AdminSeckillAdapter adapter;
    private SeckillDao seckillDao;

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
        final EditText productInput = (EditText) view.findViewById(R.id.input_seckill_product);
        final EditText priceInput = (EditText) view.findViewById(R.id.input_seckill_price);
        final EditText stockInput = (EditText) view.findViewById(R.id.input_seckill_stock);
        final EditText startInput = (EditText) view.findViewById(R.id.input_seckill_start);
        final EditText endInput = (EditText) view.findViewById(R.id.input_seckill_end);

        long now = System.currentTimeMillis();
        if (item != null) {
            productInput.setText(item.productId);
            priceInput.setText(String.valueOf(item.seckillPrice));
            stockInput.setText(String.valueOf(item.stock));
            startInput.setText(String.valueOf(item.startTime));
            endInput.setText(String.valueOf(item.endTime));
        } else {
            startInput.setText(String.valueOf(now));
            endInput.setText(String.valueOf(now + 2 * 60 * 60 * 1000));
        }

        new AlertDialog.Builder(this)
                .setTitle(item == null ? R.string.admin_seckill_add : R.string.admin_seckill_edit)
                .setView(view)
                .setPositiveButton(R.string.common_save, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String productId = productInput.getText().toString().trim();
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
                        SeckillItem updated = new SeckillItem(
                                item == null ? 0 : item.id,
                                productId,
                                Double.parseDouble(priceText),
                                Integer.parseInt(stockText),
                                Long.parseLong(startText),
                                Long.parseLong(endText),
                                item == null ? 1 : item.status,
                                item == null ? null : item.food
                        );
                        seckillDao.insertOrUpdate(updated);
                        loadItems();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
