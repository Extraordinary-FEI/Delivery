package com.example.cn.helloworld.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.AuthApiClient;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.utils.LocaleManager;
import com.example.cn.helloworld.utils.SessionManager;

public class LoginActivity extends BaseActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private RadioGroup roleGroup;
    private boolean isInitialLanguageSelection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = (EditText) findViewById(R.id.edit_username);
        passwordInput = (EditText) findViewById(R.id.edit_password);
        Button loginButton = (Button) findViewById(R.id.button_login);
        Button registerButton = (Button) findViewById(R.id.button_register);
        roleGroup = (RadioGroup) findViewById(R.id.radio_group_role);

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
        String role = resolveSelectedRole();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.login_error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthApiClient.login(this, username, password, role,
                new AuthApiClient.Callback<AuthApiClient.LoginResponse>() {
            @Override
            public void onSuccess(AuthApiClient.LoginResponse result) {
                SessionManager.saveSession(LoginActivity.this, result.getUsername(), result.getRole(),
                        result.getToken(), result.getUserId());
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String resolveSelectedRole() {
        if (roleGroup == null) {
            return getString(R.string.role_user_value);
        }
        int checkedId = roleGroup.getCheckedRadioButtonId();
        View checkedView = roleGroup.findViewById(checkedId);
        if (checkedView != null && checkedView.getTag() != null) {
            return checkedView.getTag().toString();
        }
        return getString(R.string.role_user_value);
    }
}
