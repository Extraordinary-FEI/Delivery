package com.example.cn.helloworld.ui.address;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.model.Address;
import com.example.cn.helloworld.db.AddressDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

public class AddressEditActivity extends BaseActivity {
    public static final String EXTRA_ADDRESS_ID = "extra_address_id";
    public static final String EXTRA_SELECT_AFTER_SAVE = "extra_select_after_save";

    private AddressDao addressDao;
    private EditText nameInput;
    private EditText phoneInput;
    private EditText detailInput;
    private CheckBox defaultCheck;
    private int userId;
    private int addressId;
    private boolean selectAfterSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_edit);
        setupBackButton();

        addressDao = new AddressDao(this);
        userId = parseUserId(SessionManager.getUserId(this));
        addressId = getIntent().getIntExtra(EXTRA_ADDRESS_ID, -1);
        selectAfterSave = getIntent().getBooleanExtra(EXTRA_SELECT_AFTER_SAVE, false);

        nameInput = (EditText) findViewById(R.id.input_address_name);
        phoneInput = (EditText) findViewById(R.id.input_address_phone);
        detailInput = (EditText) findViewById(R.id.input_address_detail);
        defaultCheck = (CheckBox) findViewById(R.id.checkbox_address_default);
        Button saveButton = (Button) findViewById(R.id.button_save_address);

        if (addressId > 0) {
            bindAddress();
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();
            }
        });
    }

    private void bindAddress() {
        Address address = addressDao.getAddress(userId, addressId);
        if (address == null) {
            return;
        }
        nameInput.setText(address.getContactName());
        phoneInput.setText(address.getContactPhone());
        detailInput.setText(address.getDetail());
        defaultCheck.setChecked(address.isDefault());
    }

    private void saveAddress() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String detail = detailInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameInput.setError(getString(R.string.address_error_name));
            return;
        }
        if (TextUtils.isEmpty(detail)) {
            detailInput.setError(getString(R.string.address_error_detail));
            return;
        }

        Address address = new Address(
                addressId,
                userId,
                name,
                phone,
                detail,
                defaultCheck.isChecked(),
                System.currentTimeMillis()
        );
        int savedId = addressDao.saveAddress(address);
        if (savedId > 0) {
            Toast.makeText(this, R.string.address_save_success, Toast.LENGTH_SHORT).show();
            if (selectAfterSave) {
                Intent data = new Intent();
                data.putExtra(AddressListActivity.EXTRA_SELECTED_ADDRESS_ID, savedId);
                setResult(RESULT_OK, data);
                finish();
                return;
            }
            finish();
        } else {
            Toast.makeText(this, R.string.address_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

