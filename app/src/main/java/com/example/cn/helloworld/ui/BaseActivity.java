package com.example.cn.helloworld.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.cn.helloworld.utils.LocaleManager;
import com.example.cn.helloworld.utils.ThemeManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase));
    }
}
