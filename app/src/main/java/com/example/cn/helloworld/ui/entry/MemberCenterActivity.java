package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;

public class MemberCenterActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_center);
        setupBackButton();
    }
}
