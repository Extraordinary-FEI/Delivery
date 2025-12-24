package com.example.cn.helloworld.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.utils.LocaleManager;
import com.example.cn.helloworld.utils.SessionManager;

public class LoginActivity extends BaseActivity {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private EditText usernameInput;
    private EditText passwordInput;
    private boolean isInitialLanguageSelection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = (EditText) findViewById(R.id.edit_username);
        passwordInput = (EditText) findViewById(R.id.edit_password);
        Button loginButton = (Button) findViewById(R.id.button_login);
        Button registerButton = (Button) findViewById(R.id.button_register);

        setupLanguageSelector();

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
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.login_error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedUsername = preferences.getString(KEY_USERNAME, "");
        String storedPassword = preferences.getString(KEY_PASSWORD, "");
        String storedRole = preferences.getString(KEY_ROLE, SessionManager.ROLE_USER);

        boolean isAdmin = ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
        if (!isAdmin) {
            if (!username.equals(storedUsername) || !password.equals(storedPassword)) {
                Toast.makeText(this, R.string.login_error_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String role = isAdmin ? SessionManager.ROLE_ADMIN : storedRole;
        SessionManager.saveSession(this, username, role);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
