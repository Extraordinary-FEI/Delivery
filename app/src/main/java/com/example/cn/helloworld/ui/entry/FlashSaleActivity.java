package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

public class FlashSaleActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_sale);
        setupBackButton();

        TextView adminHint = (TextView) findViewById(R.id.text_flash_sale_admin);
        if (SessionManager.isAdmin(this)) {
            adminHint.setText(R.string.flash_sale_admin_hint);
        }
    }
}

