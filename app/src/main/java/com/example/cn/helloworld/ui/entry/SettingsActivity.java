package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.auth.LoginActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.ImageLoader;
import com.example.cn.helloworld.utils.SessionManager;

public class SettingsActivity extends BaseActivity {
    private UserDao userDao;
    private ImageView avatarView;
    private TextView usernameView;
    private TextView phoneSummaryView;
    private EditText nicknameInput;
    private EditText phoneInput;
    private EditText avatarInput;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupBackButton();

        userDao = new UserDao(this);
        userId = parseUserId(SessionManager.getUserId(this));

        avatarView = findViewById(R.id.image_avatar);
        usernameView = findViewById(R.id.text_username);
        phoneSummaryView = findViewById(R.id.text_phone_summary);
        nicknameInput = findViewById(R.id.input_nickname);
        phoneInput = findViewById(R.id.input_phone);
        avatarInput = findViewById(R.id.input_avatar_url);

        Button saveButton = findViewById(R.id.button_save_profile);
        Button logoutButton = findViewById(R.id.button_logout);

        loadProfile();

        saveButton.setOnClickListener(view -> saveProfile());
        logoutButton.setOnClickListener(view -> logout());
    }

    private void loadProfile() {
        UserDao.UserProfile profile = userDao.getProfile(userId);
        if (profile == null) {
            return;
        }
        usernameView.setText(profile.username);
        nicknameInput.setText(profile.nickname);
        phoneInput.setText(profile.phone);
        avatarInput.setText(profile.avatarUrl);
        phoneSummaryView.setText(maskPhone(profile.phone));
        ImageLoader.load(this, avatarView, profile.avatarUrl);
    }

    private void saveProfile() {
        String nickname = nicknameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String avatarUrl = avatarInput.getText().toString().trim();

        if (TextUtils.isEmpty(nickname)) {
            nicknameInput.setError(getString(R.string.settings_error_nickname));
            return;
        }

        boolean updated = userDao.updateProfile(userId, nickname, phone, avatarUrl);
        if (updated) {
            phoneSummaryView.setText(maskPhone(phone));
            ImageLoader.load(this, avatarView, avatarUrl);
            Toast.makeText(this, R.string.settings_save_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.settings_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SessionManager.clearSession(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String maskPhone(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() < 7) {
            return getString(R.string.settings_phone_placeholder);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
