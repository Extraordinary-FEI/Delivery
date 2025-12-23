package com.example.cn.helloworld.ui.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cn.helloworld.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = (EditText) findViewById(R.id.edit_username);
        passwordInput = (EditText) findViewById(R.id.edit_password);
        confirmPasswordInput = (EditText) findViewById(R.id.edit_confirm_password);
        Button registerButton = (Button) findViewById(R.id.button_register_submit);
        Button backButton = (Button) findViewById(R.id.button_back_to_login);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRegister();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void handleRegister() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.register_error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, R.string.register_error_password_short, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.register_error_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .apply();

        Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}

