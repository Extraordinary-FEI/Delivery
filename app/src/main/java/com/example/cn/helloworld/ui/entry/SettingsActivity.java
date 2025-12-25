package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.address.AddressListActivity;
import com.example.cn.helloworld.ui.auth.LoginActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.ImageLoader;
import com.example.cn.helloworld.utils.SessionManager;

import java.io.File;

public class SettingsActivity extends BaseActivity {
    private UserDao userDao;
    private ImageView avatarView;
    private TextView usernameView;
    private TextView phoneSummaryView;
    private EditText nicknameInput;
    private EditText phoneInput;
    private Button changeAvatarButton;
    private int userId;
    private UserDao.UserProfile currentProfile;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri pendingCameraUri;

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
        changeAvatarButton = (Button) findViewById(R.id.button_change_avatar);

        Button saveButton = (Button) findViewById(R.id.button_save_profile);
        Button logoutButton = (Button) findViewById(R.id.button_logout);
        View addressButton = findViewById(R.id.button_manage_addresses);

        loadProfile();

        setupAvatarLaunchers();
        View avatarArea = findViewById(R.id.image_avatar);
        avatarArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAvatarPicker();
            }
        });
        changeAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAvatarPicker();
            }
        });
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
        phoneSummaryView.setText(maskPhone(currentProfile.phone));
        ImageLoader.loadAvatar(this, avatarView, currentProfile.avatarUrl);
    }

    private void saveProfile() {
        String nickname = nicknameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        if (currentProfile == null) {
            return;
        }
        if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, R.string.settings_error_nickname, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean changed = !TextUtils.equals(nickname, safeText(currentProfile.nickname))
                || !TextUtils.equals(phone, safeText(currentProfile.phone));

        if (!changed) {
            Toast.makeText(this, R.string.settings_save_no_changes, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = userDao.updateProfile(userId, nickname, phone, currentProfile.avatarUrl);
        if (updated) {
            phoneSummaryView.setText(maskPhone(phone));
            ImageLoader.loadAvatar(this, avatarView, currentProfile.avatarUrl);
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

    private void setupAvatarLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleGalleryResult
        );
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleCameraResult
        );
    }

    private void showAvatarPicker() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.avatar_picker_title)
                .setItems(new CharSequence[]{
                        getString(R.string.avatar_picker_gallery),
                        getString(R.string.avatar_picker_camera)
                }, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else if (which == 1) {
                        openCamera();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        pendingCameraUri = createCameraUri();
        if (pendingCameraUri == null) {
            Toast.makeText(this, R.string.settings_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingCameraUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cameraLauncher.launch(intent);
    }

    private Uri createCameraUri() {
        File imageFile = new File(getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
    }

    private void handleGalleryResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }
        Uri uri = result.getData().getData();
        if (uri != null) {
            updateAvatar(uri.toString());
        }
    }

    private void handleCameraResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || pendingCameraUri == null) {
            return;
        }
        updateAvatar(pendingCameraUri.toString());
    }

    private void updateAvatar(String avatarUri) {
        if (currentProfile == null) {
            return;
        }
        boolean updated = userDao.updateProfile(
                userId,
                safeText(nicknameInput.getText().toString()),
                safeText(phoneInput.getText().toString()),
                avatarUri
        );
        if (updated) {
            ImageLoader.loadAvatar(this, avatarView, avatarUri);
            currentProfile.avatarUrl = avatarUri;
            Toast.makeText(this, R.string.settings_save_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.settings_save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
