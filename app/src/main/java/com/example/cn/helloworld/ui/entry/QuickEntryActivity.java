package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;

public class QuickEntryActivity extends BaseActivity {
    public static final String EXTRA_TITLE_RES = "extra_title_res";
    public static final String EXTRA_DESC_RES = "extra_desc_res";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_entry);
        setupBackButton();

        TextView titleView = (TextView) findViewById(R.id.quick_entry_title);
        TextView descView = (TextView) findViewById(R.id.quick_entry_desc);

        int titleResId = getIntent().getIntExtra(EXTRA_TITLE_RES, R.string.quick_entry_title);
        int descResId = getIntent().getIntExtra(EXTRA_DESC_RES, R.string.quick_entry_intro);
        titleView.setText(titleResId);
        descView.setText(descResId);
    }
}
