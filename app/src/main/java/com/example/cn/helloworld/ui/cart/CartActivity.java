package com.example.cn.helloworld.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.data.model.Address;
import com.example.cn.helloworld.db.AddressDao;
import com.example.cn.helloworld.db.CouponDao;
import com.example.cn.helloworld.ui.address.AddressListActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.entry.MemberCenterActivity;
import com.example.cn.helloworld.ui.entry.CouponCenterActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.ui.market.FoodMarketActivity;
import com.example.cn.helloworld.ui.order.CheckoutActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;
import com.example.cn.helloworld.utils.AddressUtils;
import com.example.cn.helloworld.utils.SessionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity implements CartAdapter.CartActionListener {
    private static final int REQUEST_SELECT_ADDRESS = 2002;

    private RecyclerView cartRecyclerView;
    private TextView totalAmountView;
    private TextView goodsTotalView;
    private TextView payableView;
    private TextView emptyView;
    private View contentContainer;
    private TextView clearButton;
    private Button checkoutButton;
    private CheckBox selectAllCheckBox;
    private TextView addressDetailView;
    private TextView addressActionView;
    private TextView couponInfoView;
    private TextView couponActionView;
    private CartAdapter cartAdapter;
    private final List<CartItem> cartItems = new ArrayList<CartItem>();
    private CartManager cartManager;
    private AddressDao addressDao;
    private Address selectedAddress;
    private CouponDao couponDao;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupBackButton();

        cartRecyclerView = (RecyclerView) findViewById(R.id.recycler_cart);
        totalAmountView = (TextView) findViewById(R.id.text_total_amount);
        goodsTotalView = (TextView) findViewById(R.id.text_fee_goods_total);
        payableView = (TextView) findViewById(R.id.text_fee_payable);
        emptyView = (TextView) findViewById(R.id.text_empty_cart);
        contentContainer = findViewById(R.id.cart_content_container);
        clearButton = (TextView) findViewById(R.id.button_clear_cart);
        checkoutButton = (Button) findViewById(R.id.button_checkout);
        selectAllCheckBox = (CheckBox) findViewById(R.id.checkbox_select_all);
        addressDetailView = (TextView) findViewById(R.id.text_cart_address);
        addressActionView = (TextView) findViewById(R.id.text_cart_address_action);
        couponInfoView = (TextView) findViewById(R.id.text_cart_coupon_info);
        couponActionView = (TextView) findViewById(R.id.text_cart_coupon_action);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartManager = CartManager.getInstance(this);
        cartAdapter = new CartAdapter(cartItems, this);
        cartRecyclerView.setAdapter(cartAdapter);

        userId = parseUserId(SessionManager.getUserId(this));
        addressDao = new AddressDao(this);
        couponDao = new CouponDao(this);

        setupDockActions();
        refreshCartItems();
        refreshAddress();
        refreshCoupons();

        if (clearButton != null) {
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartManager.clear();
                    refreshCartItems();
                }
            });
        }

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> selectedItems = getSelectedItemNames();
                if (selectedItems.isEmpty()) {
                    updateTotal();
                    return;
                }
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putStringArrayListExtra(CheckoutActivity.EXTRA_SELECTED_ITEMS, selectedItems);
                startActivity(intent);
            }
        });

        View addressLayout = findViewById(R.id.layout_cart_address);
        if (addressLayout != null) {
            addressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddressSelector();
                }
            });
        }
        if (addressActionView != null) {
            addressActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddressSelector();
                }
            });
        }
        if (couponActionView != null) {
            couponActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CartActivity.this, CouponCenterActivity.class));
                }
            });
        }

        if (selectAllCheckBox != null) {
            selectAllCheckBox.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                    for (CartItem item : cartItems) {
                        item.setSelected(isChecked);
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateTotal();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartItems();
        refreshAddress();
        refreshCoupons();
    }

    @Override
    public void onQuantityChanged(CartItem item) {
        cartManager.updateItemQuantity(item.getName(), item.getQuantity());
        updateTotal();
    }

    @Override
    public void onItemRemoved(int position, CartItem item) {
        if (position >= 0 && position < cartItems.size()) {
            cartManager.removeItem(item.getName());
            cartItems.remove(position);
            cartAdapter.notifyItemRemoved(position);
            cartAdapter.notifyItemRangeChanged(position, cartItems.size() - position);
            updateTotal();
        }
    }

    @Override
    public void onSelectionChanged(CartItem item) {
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        int selectedCount = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                BigDecimal lineTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                total = total.add(lineTotal);
                selectedCount += item.getQuantity();
            }
        }
        String totalText = getString(R.string.cart_total_format, total.toPlainString());
        totalAmountView.setText(totalText);
        if (goodsTotalView != null) {
            goodsTotalView.setText(getString(R.string.cart_amount_format, total.toPlainString()));
        }
        if (payableView != null) {
            payableView.setText(getString(R.string.cart_amount_format, total.toPlainString()));
        }
        boolean hasItems = !cartItems.isEmpty();
        boolean hasSelection = selectedCount > 0;
        checkoutButton.setEnabled(hasSelection);
        if (clearButton != null) {
            clearButton.setEnabled(hasItems);
        }
        if (emptyView != null) {
            emptyView.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        }
        if (contentContainer != null) {
            contentContainer.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        }
        if (selectAllCheckBox != null) {
            selectAllCheckBox.setOnCheckedChangeListener(null);
            selectAllCheckBox.setChecked(hasItems && selectedCount == cartManager.getTotalCount());
            selectAllCheckBox.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                    for (CartItem item : cartItems) {
                        item.setSelected(isChecked);
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateTotal();
                }
            });
        }
    }

    private void refreshCartItems() {
        cartItems.clear();
        for (com.example.cn.helloworld.data.cart.CartItem item : cartManager.getItems()) {
            CartItem cartItem = new CartItem(
                    item.getName(),
                    BigDecimal.valueOf(item.getPrice()),
                    item.getQuantity(),
                    item.getImageUrl());
            cartItem.setSelected(true);
            cartItems.add(cartItem);
        }
        cartAdapter.notifyDataSetChanged();
        updateTotal();
    }

    private void refreshAddress() {
        selectedAddress = addressDao.getDefaultAddress(userId);
        if (selectedAddress == null) {
            if (addressDetailView != null) {
                addressDetailView.setText(R.string.checkout_address_empty);
            }
            if (addressActionView != null) {
                addressActionView.setText(R.string.checkout_address_add);
            }
            return;
        }
        String detail = getString(R.string.address_detail_format,
                AddressUtils.buildFullAddress(selectedAddress),
                safeText(selectedAddress.getContactName()),
                maskPhone(selectedAddress.getContactPhone()));
        if (addressDetailView != null) {
            addressDetailView.setText(detail);
        }
        if (addressActionView != null) {
            addressActionView.setText(R.string.checkout_address_change);
        }
    }

    private void refreshCoupons() {
        if (couponDao == null || couponInfoView == null) {
            return;
        }
        int count = couponDao.getCouponCount(userId);
        if (count <= 0) {
            couponInfoView.setText(R.string.cart_coupon_empty);
            return;
        }
        couponInfoView.setText(getString(R.string.cart_coupon_available_format, count));
    }

    private void openAddressSelector() {
        Intent intent = new Intent(this, AddressListActivity.class);
        intent.putExtra(AddressListActivity.EXTRA_SELECT_MODE, true);
        startActivityForResult(intent, REQUEST_SELECT_ADDRESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_ADDRESS && resultCode == RESULT_OK && data != null) {
            int addressId = data.getIntExtra(AddressListActivity.EXTRA_SELECTED_ADDRESS_ID, -1);
            if (addressId > 0) {
                selectedAddress = addressDao.getAddress(userId, addressId);
                refreshAddress();
            }
        }
    }

    private ArrayList<String> getSelectedItemNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                result.add(item.getName());
            }
        }
        return result;
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
        return value == null ? "" : value.trim();
    }

    private void setupDockActions() {
        findViewById(R.id.dock_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.dock_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, ShopListActivity.class));
            }
        });

        findViewById(R.id.dock_market).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, FoodMarketActivity.class));
            }
        });

        findViewById(R.id.dock_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on cart.
            }
        });

        findViewById(R.id.dock_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, MemberCenterActivity.class));
            }
        });
    }
}
