package com.example.cn.helloworld.ui.shop.admin;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.CategoryDao;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryActivity extends BaseActivity {
    private final List<String> categories = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private CategoryDao categoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);
        setupBackButton();

        categoryDao = new CategoryDao(this);
        ListView listView = (ListView) findViewById(R.id.list_categories);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String category = categories.get(position);
                Intent intent = new Intent(AdminCategoryActivity.this, AdminCategoryFoodsActivity.class);
                intent.putExtra(AdminCategoryFoodsActivity.EXTRA_CATEGORY_NAME, category);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_add_category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        loadCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        categories.clear();
        categories.addAll(categoryDao.getCategoryNames());
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        final EditText input = new EditText(this);
        input.setHint(R.string.admin_category_name_hint);
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_category_add)
                .setView(input)
                .setPositiveButton(R.string.common_save, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(AdminCategoryActivity.this,
                                    R.string.admin_category_name_required, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!categoryDao.insertCategory(name)) {
                            Toast.makeText(AdminCategoryActivity.this,
                                    R.string.admin_category_name_duplicate, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loadCategories();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
