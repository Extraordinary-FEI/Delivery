package com.example.cn.helloworld.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.data.model.Address;
import com.example.cn.helloworld.db.AddressDao;
import com.example.cn.helloworld.ui.address.AddressListActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CheckoutActivity extends BaseActivity {
    private static final int REQUEST_SELECT_ADDRESS = 2001;

    private AddressDao addressDao;
    private Address selectedAddress;
    private TextView addressDetailView;
    private TextView addressActionView;
    private Button placeOrderButton;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setupBackButton();

        TextView titleView = (TextView) findViewById(R.id.text_checkout_title);
        titleView.setText(R.string.checkout_title);

        userId = parseUserId(SessionManager.getUserId(this));
        addressDao = new AddressDao(this);

        CartManager cartManager = CartManager.getInstance(this);
        TextView totalCountView = (TextView) findViewById(R.id.text_checkout_total_count);
        TextView totalAmountView = (TextView) findViewById(R.id.text_checkout_total_amount);
        addressDetailView = (TextView) findViewById(R.id.text_checkout_address_detail);
        addressActionView = (TextView) findViewById(R.id.text_checkout_address_action);
        placeOrderButton = (Button) findViewById(R.id.button_place_order);
        View addressLayout = findViewById(R.id.layout_checkout_address);

        totalCountView.setText(getString(R.string.checkout_item_count_format, cartManager.getTotalCount()));
        BigDecimal totalAmount = BigDecimal.valueOf(cartManager.getTotalPrice());
        totalAmountView.setText(getString(R.string.cart_total_format, totalAmount.toPlainString()));

        addressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddressSelector();
            }
        });
        addressActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddressSelector();
            }
        });
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrder();
            }
        });

        selectedAddress = addressDao.getDefaultAddress(userId);
        refreshAddress();
        updatePlaceOrderState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedAddress = addressDao.getDefaultAddress(userId);
        refreshAddress();
        updatePlaceOrderState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_ADDRESS && resultCode == RESULT_OK && data != null) {
            int addressId = data.getIntExtra(AddressListActivity.EXTRA_SELECTED_ADDRESS_ID, -1);
            if (addressId > 0) {
                selectedAddress = addressDao.getAddress(userId, addressId);
                refreshAddress();
                updatePlaceOrderState();
            }
        }
    }

    private void refreshAddress() {
        if (selectedAddress == null) {
            addressDetailView.setText(R.string.checkout_address_empty);
            addressActionView.setText(R.string.checkout_address_add);
            return;
        }
        String detail = getString(R.string.address_detail_format,
                safeText(selectedAddress.getDetail()),
                safeText(selectedAddress.getContactName()),
                maskPhone(selectedAddress.getContactPhone()));
        addressDetailView.setText(detail);
        addressActionView.setText(R.string.checkout_address_change);
    }

    private void updatePlaceOrderState() {
        boolean hasAddress = selectedAddress != null;
        boolean hasItems = CartManager.getInstance(this).getTotalCount() > 0;
        placeOrderButton.setEnabled(hasAddress && hasItems);
    }

    private void openAddressSelector() {
        Intent intent = new Intent(this, AddressListActivity.class);
        intent.putExtra(AddressListActivity.EXTRA_SELECT_MODE, true);
        startActivityForResult(intent, REQUEST_SELECT_ADDRESS);
    }

    private void submitOrder() {
        CartManager cartManager = CartManager.getInstance(this);
        if (selectedAddress == null) {
            openAddressSelector();
            return;
        }
        if (cartManager.getTotalCount() == 0) {
            updatePlaceOrderState();
            return;
        }
        ArrayList<String> items = new ArrayList<String>();
        for (com.example.cn.helloworld.data.cart.CartItem item : cartManager.getItems()) {
            items.add(item.getName() + " x" + item.getQuantity());
        }
        String orderId = "OD" + System.currentTimeMillis();
        String orderTime = new SimpleDateFormat(\"yyyy-MM-dd HH:mm\", Locale.getDefault()).format(new Date());
        Intent intent = new Intent(this, OrderConfirmActivity.class);
        intent.putExtra(OrderConfirmActivity.EXTRA_ORDER_ID, orderId);
        intent.putExtra(OrderConfirmActivity.EXTRA_ORDER_TIME, orderTime);
        intent.putStringArrayListExtra(OrderConfirmActivity.EXTRA_ORDER_ITEMS, items);
        intent.putExtra(OrderConfirmActivity.EXTRA_ORDER_TOTAL, cartManager.getTotalPrice());
        startActivity(intent);
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return getString(R.string.address_phone_placeholder);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String safeText(String value) {
        return value == null ? \"\" : value.trim();
    }
}
