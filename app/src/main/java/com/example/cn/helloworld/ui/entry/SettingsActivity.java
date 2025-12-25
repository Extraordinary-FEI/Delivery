package com.example.cn.helloworld.ui.entry;

import android.content.DialogInterface;
import android.app.DatePickerDialog;
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

import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.address.AddressListActivity;
import com.example.cn.helloworld.ui.auth.LoginActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.AvatarLoader;
import com.example.cn.helloworld.utils.SessionManager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends BaseActivity {
    private UserDao userDao;
    private ImageView avatarView;
    private TextView usernameView;
    private TextView phoneSummaryView;
    private TextView birthdayView;
    private EditText nicknameInput;
    private EditText phoneInput;
    private Button changeAvatarButton;
    private int userId;
    private UserDao.UserProfile currentProfile;
    private final SimpleDateFormat birthdayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_TAKE_PHOTO = 1002;
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
        birthdayView = (TextView) findViewById(R.id.text_birthday_value);
        nicknameInput = (EditText) findViewById(R.id.input_nickname);
        phoneInput = (EditText) findViewById(R.id.input_phone);
        changeAvatarButton = (Button) findViewById(R.id.button_change_avatar);

        Button saveButton = (Button) findViewById(R.id.button_save_profile);
        Button logoutButton = (Button) findViewById(R.id.button_logout);
        View addressButton = findViewById(R.id.button_manage_addresses);

        loadProfile();

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
        birthdayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBirthdayPicker();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
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
        birthdayView.setText(formatBirthday(currentProfile.birthday));
        AvatarLoader.load(this, avatarView, currentProfile.avatarUrl);
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
            AvatarLoader.load(this, avatarView, currentProfile.avatarUrl);
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

    private void showAvatarPicker() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.avatar_picker_title)
                .setItems(new CharSequence[]{
                        getString(R.string.avatar_picker_gallery),
                        getString(R.string.avatar_picker_camera)
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openGallery();
                        } else if (which == 1) {
                            openCamera();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
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
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private Uri createCameraUri() {
        File imageFile = new File(getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PICK_IMAGE) {
            handleGalleryResult(data);
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            handleCameraResult();
        }
    }

    private void handleGalleryResult(Intent data) {
        if (data == null) {
            return;
        }
        Uri uri = data.getData();
        if (uri != null) {
            updateAvatar(uri.toString());
        }
    }

    private void handleCameraResult() {
        if (pendingCameraUri == null) {
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
            AvatarLoader.load(this, avatarView, avatarUri);
            currentProfile.avatarUrl = avatarUri;
            Toast.makeText(this, R.string.settings_save_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.settings_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showBirthdayPicker() {
        if (currentProfile == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        String birthday = currentProfile.birthday;
        if (!TextUtils.isEmpty(birthday)) {
            try {
                Date parsed = birthdayFormat.parse(birthday);
                if (parsed != null) {
                    calendar.setTime(parsed);
                }
            } catch (ParseException ignored) {
                // Use current date as fallback.
            }
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                Calendar selected = Calendar.getInstance();
                selected.set(selectedYear, selectedMonth, selectedDay);
                String formatted = birthdayFormat.format(selected.getTime());
                if (userDao.updateBirthday(userId, formatted)) {
                    currentProfile.birthday = formatted;
                    birthdayView.setText(formatted);
                    Toast.makeText(SettingsActivity.this, R.string.settings_birthday_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.settings_save_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }, year, month, day);
        dialog.show();
    }

    private String formatBirthday(String birthday) {
        if (TextUtils.isEmpty(birthday)) {
            return getString(R.string.settings_birthday_placeholder);
        }
        return birthday;
    }

    private void showBirthdayPicker() {
        if (currentProfile == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        String birthday = currentProfile.birthday;
        if (!TextUtils.isEmpty(birthday)) {
            try {
                Date parsed = birthdayFormat.parse(birthday);
                if (parsed != null) {
                    calendar.setTime(parsed);
                }
            } catch (ParseException ignored) {
                // Use current date as fallback.
            }
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                Calendar selected = Calendar.getInstance();
                selected.set(selectedYear, selectedMonth, selectedDay);
                String formatted = birthdayFormat.format(selected.getTime());
                if (userDao.updateBirthday(userId, formatted)) {
                    currentProfile.birthday = formatted;
                    birthdayView.setText(formatted);
                    Toast.makeText(SettingsActivity.this, R.string.settings_birthday_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.settings_save_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }, year, month, day);
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private String formatBirthday(String birthday) {
        if (TextUtils.isEmpty(birthday)) {
            return getString(R.string.settings_birthday_placeholder);
        }
        return birthday;
    }
}
