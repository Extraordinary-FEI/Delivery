package com.example.cn.helloworld.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = (EditText) findViewById(R.id.edit_username);
        passwordInput = (EditText) findViewById(R.id.edit_password);
        Button loginButton = (Button) findViewById(R.id.button_login);
        Button registerButton = (Button) findViewById(R.id.button_register);

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

        if (!username.equals(storedUsername) || !password.equals(storedPassword)) {
            Toast.makeText(this, R.string.login_error_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

