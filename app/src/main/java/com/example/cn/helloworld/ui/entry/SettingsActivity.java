package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.address.AddressListActivity;
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
    private UserDao.UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupBackButton();

        userDao = new UserDao(this);
        userId = parseUserId(SessionManager.getUserId(this));

        avatarView = (ImageView) findViewById(R.id.image_avatar);
        usernameView = (TextView) findViewById(R.id.text_username);
        phoneSummaryView = (TextView) findViewById(R.id.text_phone_summary);
        nicknameInput = (EditText) findViewById(R.id.input_nickname);
        phoneInput = (EditText) findViewById(R.id.input_phone);
        avatarInput = (EditText) findViewById(R.id.input_avatar_url);

        Button saveButton = (Button) findViewById(R.id.button_save_profile);
        Button logoutButton = (Button) findViewById(R.id.button_logout);
        View addressButton = findViewById(R.id.button_manage_addresses);

        loadProfile();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, AddressListActivity.class));
            }
        });
    }

    private void loadProfile() {
        currentProfile = userDao.getProfile(userId);
        if (currentProfile == null) {
            return;
        }
        usernameView.setText(currentProfile.username);
        nicknameInput.setText(currentProfile.nickname);
        phoneInput.setText(currentProfile.phone);
        avatarInput.setText(currentProfile.avatarUrl);
        phoneSummaryView.setText(maskPhone(currentProfile.phone));
        ImageLoader.load(this, avatarView, currentProfile.avatarUrl);
    }

    private void saveProfile() {
        String nickname = nicknameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String avatarUrl = avatarInput.getText().toString().trim();

        if (currentProfile == null) {
            return;
        }

        boolean changed = !TextUtils.equals(nickname, safeText(currentProfile.nickname))
                || !TextUtils.equals(phone, safeText(currentProfile.phone))
                || !TextUtils.equals(avatarUrl, safeText(currentProfile.avatarUrl));

        if (!changed) {
            Toast.makeText(this, R.string.settings_save_no_changes, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = userDao.updateProfile(userId, nickname, phone, avatarUrl);
        if (updated) {
            phoneSummaryView.setText(maskPhone(phone));
            ImageLoader.load(this, avatarView, avatarUrl);
            Toast.makeText(this, R.string.settings_save_success, Toast.LENGTH_SHORT).show();
            loadProfile();
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

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
