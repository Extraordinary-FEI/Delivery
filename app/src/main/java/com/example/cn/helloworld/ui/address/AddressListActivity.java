package com.example.cn.helloworld.ui.address;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.model.Address;
import com.example.cn.helloworld.db.AddressDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AddressListActivity extends BaseActivity implements AddressAdapter.AddressActionListener {
    public static final String EXTRA_SELECT_MODE = "extra_select_mode";
    public static final String EXTRA_SELECTED_ADDRESS_ID = "extra_selected_address_id";
    public static final int REQUEST_ADD_ADDRESS = 1001;
    public static final int REQUEST_EDIT_ADDRESS = 1002;

    private final List<Address> addresses = new ArrayList<Address>();
    private AddressAdapter adapter;
    private AddressDao addressDao;
    private TextView emptyView;
    private boolean selectionMode;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);
        setupBackButton();

        selectionMode = getIntent().getBooleanExtra(EXTRA_SELECT_MODE, false);
        userId = parseUserId(SessionManager.getUserId(this));
        addressDao = new AddressDao(this);

        TextView titleView = (TextView) findViewById(R.id.text_address_title);
        titleView.setText(selectionMode
                ? getString(R.string.address_select_title)
                : getString(R.string.address_manage_title));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_addresses);
        emptyView = (TextView) findViewById(R.id.text_address_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddressAdapter(addresses, this);
        adapter.setSelectionMode(selectionMode);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.button_add_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressListActivity.this, AddressEditActivity.class);
                intent.putExtra(AddressEditActivity.EXTRA_SELECT_AFTER_SAVE, selectionMode);
                startActivityForResult(intent, REQUEST_ADD_ADDRESS);
            }
        });

        loadAddresses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!selectionMode || resultCode != RESULT_OK || data == null) {
            return;
        }
        int addressId = data.getIntExtra(EXTRA_SELECTED_ADDRESS_ID, -1);
        if (addressId > 0) {
            addressDao.setDefaultAddress(userId, addressId);
            Intent result = new Intent();
            result.putExtra(EXTRA_SELECTED_ADDRESS_ID, addressId);
            setResult(RESULT_OK, result);
            finish();
        }
    }

    private void loadAddresses() {
        addresses.clear();
        addresses.addAll(addressDao.getAddresses(userId));
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSelect(Address address) {
        if (address == null) {
            return;
        }
        addressDao.setDefaultAddress(userId, address.getId());
        Intent data = new Intent();
        data.putExtra(EXTRA_SELECTED_ADDRESS_ID, address.getId());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onEdit(Address address) {
        if (address == null) {
            return;
        }
        Intent intent = new Intent(this, AddressEditActivity.class);
        intent.putExtra(AddressEditActivity.EXTRA_ADDRESS_ID, address.getId());
        startActivityForResult(intent, REQUEST_EDIT_ADDRESS);
    }

    @Override
    public void onDelete(Address address) {
        if (address == null) {
            return;
        }
        boolean deleted = addressDao.deleteAddress(userId, address.getId());
        if (deleted) {
            Toast.makeText(this, R.string.address_delete_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.address_delete_failed, Toast.LENGTH_SHORT).show();
        }
        loadAddresses();
    }

    @Override
    public void onSetDefault(Address address) {
        if (address == null) {
            return;
        }
        addressDao.setDefaultAddress(userId, address.getId());
        Toast.makeText(this, R.string.address_default_set, Toast.LENGTH_SHORT).show();
        loadAddresses();
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

