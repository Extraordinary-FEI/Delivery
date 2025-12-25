package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.ImageLoader;
import com.example.cn.helloworld.utils.SessionManager;

public class MemberCenterActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_center);
        setupBackButton();

        findViewById(R.id.button_settings).setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        bindProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindProfile();
    }

    private void bindProfile() {
        String userIdText = SessionManager.getUserId(this);
        int userId;
        try {
            userId = Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            userId = -1;
        }
        UserDao userDao = new UserDao(this);
        UserDao.UserProfile profile = userDao.getProfile(userId);
        if (profile == null) {
            return;
        }
        TextView usernameView = findViewById(R.id.text_username);
        ImageView avatarView = findViewById(R.id.image_avatar);
        String displayName = profile.nickname == null || profile.nickname.trim().isEmpty()
                ? profile.username
                : profile.nickname;
        usernameView.setText(displayName);
        ImageLoader.load(this, avatarView, profile.avatarUrl);
    }
}
