package com.example.cn.helloworld.ui.common;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.utils.LocaleManager;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase));
    }

    protected void setupBackButton() {
        View backButton = findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }
}
