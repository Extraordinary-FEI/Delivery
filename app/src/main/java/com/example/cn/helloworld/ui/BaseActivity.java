package com.example.cn.helloworld.ui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.example.cn.helloworld.utils.LocaleManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase));
    }
}
