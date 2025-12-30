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
import com.example.cn.helloworld.db.CouponDao;
import com.example.cn.helloworld.db.OrderDao;
import com.example.cn.helloworld.ui.address.AddressListActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.AddressUtils;
import com.example.cn.helloworld.utils.SessionManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends BaseActivity {
    private static final int REQUEST_SELECT_ADDRESS = 2001;
    public static final String EXTRA_SELECTED_ITEMS = "extra_selected_items";

    private AddressDao addressDao;
    private Address selectedAddress;
    private TextView addressDetailView;
    private TextView addressActionView;
    private TextView couponNameView;
    private TextView couponRuleView;
    private TextView couponDiscountView;
    private TextView payAmountView;
    private TextView totalCountView;
    private TextView totalAmountView;
    private Button placeOrderButton;
    private int userId;
    private CouponDao couponDao;
    private OrderDao orderDao;
    private CouponOffer appliedOffer;
    private double totalAmount;
    private final List<com.example.cn.helloworld.data.cart.CartItem> checkoutItems =
            new ArrayList<com.example.cn.helloworld.data.cart.CartItem>();
    private ArrayList<String> selectedItemNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setupBackButton();

        TextView titleView = (TextView) findViewById(R.id.text_checkout_title);
        titleView.setText(R.string.checkout_title);

        userId = parseUserId(SessionManager.getUserId(this));
        addressDao = new AddressDao(this);
        couponDao = new CouponDao(this);
        orderDao = new OrderDao(this);

        CartManager cartManager = CartManager.getInstance(this);
        totalCountView = (TextView) findViewById(R.id.text_checkout_total_count);
        totalAmountView = (TextView) findViewById(R.id.text_checkout_total_amount);
        couponDiscountView = (TextView) findViewById(R.id.text_checkout_coupon_discount);
        payAmountView = (TextView) findViewById(R.id.text_checkout_pay_amount);
        couponNameView = (TextView) findViewById(R.id.text_checkout_coupon_name);
        couponRuleView = (TextView) findViewById(R.id.text_checkout_coupon_rule);
        addressDetailView = (TextView) findViewById(R.id.text_checkout_address_detail);
        addressActionView = (TextView) findViewById(R.id.text_checkout_address_action);
        placeOrderButton = (Button) findViewById(R.id.button_place_order);
        View addressLayout = findViewById(R.id.layout_checkout_address);

        selectedItemNames = getIntent().getStringArrayListExtra(EXTRA_SELECTED_ITEMS);
        refreshCheckoutItems(cartManager);

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
        refreshCoupons();
        updatePlaceOrderState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCheckoutItems(CartManager.getInstance(this));
        selectedAddress = addressDao.getDefaultAddress(userId);
        refreshAddress();
        refreshCoupons();
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
                AddressUtils.buildFullAddress(selectedAddress),
                safeText(selectedAddress.getContactName()),
                maskPhone(selectedAddress.getContactPhone()));
        addressDetailView.setText(detail);
        addressActionView.setText(R.string.checkout_address_change);
    }

    private void updatePlaceOrderState() {
        boolean hasAddress = selectedAddress != null;
        boolean hasItems = !checkoutItems.isEmpty();
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
        if (checkoutItems.isEmpty()) {
            updatePlaceOrderState();
            return;
        }
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<OrderDao.OrderItem> orderItems = new ArrayList<OrderDao.OrderItem>();
        for (com.example.cn.helloworld.data.cart.CartItem item : checkoutItems) {
            items.add(item.getName() + " x" + item.getQuantity());
            double subtotal = item.getSubtotal();
            orderItems.add(new OrderDao.OrderItem(
                    item.getName(),
                    item.getName(),
                    item.getImageUrl(),
                    item.getQuantity(),
                    item.getPrice(),
                    subtotal));
        }
        String orderId = "OD" + System.currentTimeMillis();
        String orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        CouponOffer offer = appliedOffer == null ? new CouponOffer("", "", 0, 0, false) : appliedOffer;
        boolean canUse = offer.canUse(totalAmount);
        double discount = canUse ? offer.discount : 0;
        double payAmount = Math.max(0, totalAmount - discount);
        String addressDetail = AddressUtils.buildFullAddress(selectedAddress);
        String contactName = safeText(selectedAddress.getContactName());
        String contactPhone = safeText(selectedAddress.getContactPhone());
        orderDao.insertOrder(userId, orderId, totalAmount, payAmount, canUse ? offer.name : "",
                discount,
                addressDetail, contactName, contactPhone, OrderDao.STATUS_PENDING_PAY, orderItems);
        Intent intent = new Intent(this, OrderConfirmActivity.class);
        intent.putExtra(OrderConfirmActivity.EXTRA_ORDER_ID, orderId);
        intent.putExtra(OrderConfirmActivity.EXTRA_ORDER_TIME, orderTime);
        intent.putStringArrayListExtra(OrderConfirmActivity.EXTRA_ORDER_ITEMS, items);
        intent.putExtra(OrderConfirmActivity.EXTRA_ORDER_TOTAL, payAmount);
        intent.putExtra(OrderConfirmActivity.EXTRA_USER_ID, userId);
        if (selectedItemNames == null || selectedItemNames.isEmpty()) {
            cartManager.clear();
        } else {
            for (com.example.cn.helloworld.data.cart.CartItem item : checkoutItems) {
                cartManager.removeItem(item.getName());
            }
        }
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
        return value == null ? "" : value.trim();
    }

    private void refreshCoupons() {
        List<CouponDao.Coupon> coupons = couponDao.getCoupons(userId);
        CouponOffer offer = resolveBestOffer(coupons, totalAmount);
        appliedOffer = offer;
        if (offer == null || offer.name == null || offer.name.trim().isEmpty()) {
            couponNameView.setText(R.string.checkout_coupon_empty);
            couponRuleView.setText(R.string.checkout_coupon_rule_placeholder);
            couponDiscountView.setText(R.string.checkout_coupon_discount_none);
            payAmountView.setText(getString(R.string.checkout_pay_amount_format, totalAmount));
            return;
        }
        boolean canUse = offer.canUse(totalAmount);
        couponNameView.setText(getString(R.string.checkout_coupon_name_format, offer.name,
                canUse ? getString(R.string.checkout_coupon_available) : getString(R.string.checkout_coupon_unavailable)));
        couponRuleView.setText(offer.ruleText);
        double discount = canUse ? offer.discount : 0;
        if (discount > 0) {
            couponDiscountView.setText(getString(R.string.checkout_coupon_discount_format, discount));
        } else {
            couponDiscountView.setText(R.string.checkout_coupon_discount_none);
        }
        double payAmount = Math.max(0, totalAmount - discount);
        payAmountView.setText(getString(R.string.checkout_pay_amount_format, payAmount));
    }

    private void refreshCheckoutItems(CartManager cartManager) {
        checkoutItems.clear();
        int totalCount = 0;
        double total = 0;
        List<com.example.cn.helloworld.data.cart.CartItem> items = cartManager.getItems();
        for (com.example.cn.helloworld.data.cart.CartItem item : items) {
            if (selectedItemNames == null || selectedItemNames.isEmpty()
                    || selectedItemNames.contains(item.getName())) {
                checkoutItems.add(item);
                totalCount += item.getQuantity();
                total += item.getSubtotal();
            }
        }
        totalAmount = total;
        if (totalCountView != null) {
            totalCountView.setText(getString(R.string.checkout_item_count_format, totalCount));
        }
        if (totalAmountView != null) {
            BigDecimal totalValue = BigDecimal.valueOf(totalAmount);
            totalAmountView.setText(getString(R.string.cart_total_format, totalValue.toPlainString()));
        }
    }

    private CouponOffer resolveBestOffer(List<CouponDao.Coupon> coupons, double total) {
        if (coupons == null || coupons.isEmpty()) {
            return null;
        }
        CouponOffer best = null;
        for (CouponDao.Coupon coupon : coupons) {
            CouponOffer offer = buildOffer(coupon);
            if (offer == null) {
                continue;
            }
            if (offer.canUse(total)) {
                if (best == null || offer.discount > best.discount) {
                    best = offer;
                }
            } else if (best == null) {
                best = offer;
            }
        }
        return best;
    }

    private CouponOffer buildOffer(CouponDao.Coupon coupon) {
        if (coupon == null || coupon.name == null) {
            return null;
        }
        String name = coupon.name;
        if (name.contains("5元")) {
            return new CouponOffer(name, getString(R.string.checkout_coupon_rule_5), 5, 20, true);
        }
        if (name.contains("免配送")) {
            return new CouponOffer(name, getString(R.string.checkout_coupon_rule_delivery), 3, 15, true);
        }
        if (name.contains("会员日")) {
            return new CouponOffer(name, getString(R.string.checkout_coupon_rule_member), 8, 40, true);
        }
        return new CouponOffer(name, getString(R.string.checkout_coupon_rule_default), 2, 10, true);
    }

    private static class CouponOffer {
        final String name;
        final String ruleText;
        final double discount;
        final double minimum;
        final boolean usable;

        CouponOffer(String name, String ruleText, double discount, double minimum, boolean usable) {
            this.name = name;
            this.ruleText = ruleText;
            this.discount = discount;
            this.minimum = minimum;
            this.usable = usable;
        }

        boolean canUse(double total) {
            return usable && total >= minimum;
        }
    }
}
