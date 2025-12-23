package com.example.cn.helloworld.ui.common;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.example.cn.helloworld.utils.LocaleManager;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase));
    }
}
