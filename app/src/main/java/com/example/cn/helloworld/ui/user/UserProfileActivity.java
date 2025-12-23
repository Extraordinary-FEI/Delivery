package com.example.cn.helloworld.ui.user;

import android.os.Bundle;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;

public class UserProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setupBackButton();

        TextView titleView = (TextView) findViewById(R.id.profile_title);
        titleView.setText(R.string.title_user_profile);
    }
}
