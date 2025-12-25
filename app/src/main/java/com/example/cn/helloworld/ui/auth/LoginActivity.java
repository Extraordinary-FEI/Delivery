package com.example.cn.helloworld.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.utils.LocaleManager;
import com.example.cn.helloworld.utils.SessionManager;

public class LoginActivity extends BaseActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText adminCodeInput;
    private TextView errorText;
    private TextView adminToggle;
    private View adminPanel;
    private boolean isInitialLanguageSelection = true;
    private boolean isAdminMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = (EditText) findViewById(R.id.edit_username);
        passwordInput = (EditText) findViewById(R.id.edit_password);
        adminCodeInput = (EditText) findViewById(R.id.edit_admin_code);
        errorText = (TextView) findViewById(R.id.text_login_error);
        adminToggle = (TextView) findViewById(R.id.text_admin_toggle);
        adminPanel = findViewById(R.id.layout_admin_panel);
        Button loginButton = (Button) findViewById(R.id.button_login);
        Button registerButton = (Button) findViewById(R.id.button_register);

        setupLanguageSelector();
        setupAdminToggle();
        setupInlineFeedback();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupLanguageSelector() {
        Spinner languageSpinner = (Spinner) findViewById(R.id.spinner_language);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.language_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        final String[] languageValues = getResources().getStringArray(R.array.language_option_values);
        String savedLanguage = LocaleManager.getSavedLanguage(this);
        int selectedIndex = 0;
        for (int i = 0; i < languageValues.length; i++) {
            if (languageValues[i].equals(savedLanguage)) {
                selectedIndex = i;
                break;
            }
        }
        languageSpinner.setSelection(selectedIndex);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialLanguageSelection) {
                    isInitialLanguageSelection = false;
                    return;
                }

                String language = languageValues[position];
                if (!language.equals(LocaleManager.getSavedLanguage(LoginActivity.this))) {
                    LocaleManager.setNewLocale(LoginActivity.this, language);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void handleLogin() {
        clearError();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = isAdminMode ? SessionManager.ROLE_ADMIN : SessionManager.ROLE_USER;
        String adminCode = adminCodeInput == null ? "" : adminCodeInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.login_error_empty_fields));
            return;
        }

        if (SessionManager.ROLE_ADMIN.equals(role) && adminCode.isEmpty()) {
            showError(getString(R.string.login_error_admin_code_required));
            return;
        }

        UserDao userDao = new UserDao(this);
        UserDao.LoginResult result = userDao.login(username, password, role, adminCode);
        if (!result.ok) {
            showError(result.msg);
            return;
        }

        SessionManager.saveSession(this, result.username, result.role, "", String.valueOf(result.userId));
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupAdminToggle() {
        if (adminToggle == null || adminPanel == null || adminCodeInput == null) {
            return;
        }
        adminToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdminMode = !isAdminMode;
                adminPanel.setVisibility(isAdminMode ? View.VISIBLE : View.GONE);
                adminToggle.setText(isAdminMode
                        ? R.string.action_back_to_user_login
                        : R.string.action_admin_login);
                if (!isAdminMode) {
                    adminCodeInput.setText("");
                }
                clearError();
            }
        });
        adminPanel.setVisibility(View.GONE);
    }

    private void setupInlineFeedback() {
        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    clearError();
                }
            }
        };
        usernameInput.setOnFocusChangeListener(focusListener);
        passwordInput.setOnFocusChangeListener(focusListener);
        if (adminCodeInput != null) {
            adminCodeInput.setOnFocusChangeListener(focusListener);
        }
    }

    private void showError(String message) {
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearError() {
        if (errorText != null) {
            errorText.setText("");
            errorText.setVisibility(View.GONE);
        }
    }
}
