package com.example.cn.helloworld.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

public class RegisterActivity extends BaseActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private EditText adminCodeInput;
    private RadioGroup roleGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupBackButton();

        usernameInput = (EditText) findViewById(R.id.edit_username);
        passwordInput = (EditText) findViewById(R.id.edit_password);
        confirmPasswordInput = (EditText) findViewById(R.id.edit_confirm_password);
        adminCodeInput = (EditText) findViewById(R.id.edit_admin_code);
        roleGroup = (RadioGroup) findViewById(R.id.radio_group_role);
        Button registerButton = (Button) findViewById(R.id.button_register_submit);
        Button backButton = (Button) findViewById(R.id.button_back_to_login);

        setupRoleToggle();

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
        String role = resolveSelectedRole();
        String adminCode = adminCodeInput == null ? "" : adminCodeInput.getText().toString().trim();

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

        if (SessionManager.ROLE_ADMIN.equals(role)) {
            if (adminCode.isEmpty()) {
                Toast.makeText(this, R.string.register_error_admin_code_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!UserDao.ADMIN_CODE.equals(adminCode)) {
                Toast.makeText(this, R.string.register_error_admin_code_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        UserDao userDao = new UserDao(this);
        boolean success = userDao.register(username, password, role, adminCode);
        if (success) {
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.register_error_failed, Toast.LENGTH_SHORT).show();
        }
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

    private void setupRoleToggle() {
        if (roleGroup == null || adminCodeInput == null) {
            return;
        }
        roleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String role = resolveSelectedRole();
                boolean showAdminCode = SessionManager.ROLE_ADMIN.equals(role);
                adminCodeInput.setVisibility(showAdminCode ? View.VISIBLE : View.GONE);
                if (!showAdminCode) {
                    adminCodeInput.setText("");
                }
            }
        });
        String role = resolveSelectedRole();
        adminCodeInput.setVisibility(SessionManager.ROLE_ADMIN.equals(role) ? View.VISIBLE : View.GONE);
    }
}
