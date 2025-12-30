package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.CouponDao;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CouponCenterActivity extends BaseActivity {
    private static final String BIRTHDAY_COUPON_NAME = "生日优惠券";
    private LinearLayout couponListLayout;
    private TextView couponEmptyView;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_center);
        setupBackButton();

        TextView adminHint = (TextView) findViewById(R.id.text_coupon_admin);
        if (SessionManager.isAdmin(this)) {
            adminHint.setText(R.string.coupon_admin_hint);
        }

        couponListLayout = (LinearLayout) findViewById(R.id.layout_coupon_owned_list);
        couponEmptyView = (TextView) findViewById(R.id.text_coupon_owned_empty);

        userId = parseUserId(SessionManager.getUserId(this));
        if (userId > 0) {
            maybeGrantBirthdayCoupon(userId);
        }
        renderUserCoupons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderUserCoupons();
    }

    private void maybeGrantBirthdayCoupon(int userId) {
        UserDao userDao = new UserDao(this);
        CouponDao couponDao = new CouponDao(this);
        String birthday = userDao.getBirthday(userId);
        if (TextUtils.isEmpty(birthday)) {
            return;
        }
        Calendar now = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            birth.setTime(format.parse(birthday));
        } catch (ParseException e) {
            return;
        }
        if (now.get(Calendar.MONTH) != birth.get(Calendar.MONTH)) {
            return;
        }
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        monthStart.set(Calendar.MILLISECOND, 0);
        if (couponDao.hasCouponSince(userId, BIRTHDAY_COUPON_NAME, monthStart.getTimeInMillis())) {
            return;
        }
        couponDao.insertCoupon(userId, BIRTHDAY_COUPON_NAME, 0);
        Toast.makeText(this, R.string.coupon_birthday_granted, Toast.LENGTH_SHORT).show();
    }

    private void renderUserCoupons() {
        if (couponListLayout == null || couponEmptyView == null || userId <= 0) {
            return;
        }
        CouponDao couponDao = new CouponDao(this);
        List<CouponDao.Coupon> coupons = couponDao.getCoupons(userId);
        couponListLayout.removeAllViews();
        if (coupons == null || coupons.isEmpty()) {
            couponEmptyView.setVisibility(View.VISIBLE);
            return;
        }
        couponEmptyView.setVisibility(View.GONE);
        int margin = dpToPx(8);
        int padding = dpToPx(14);
        for (CouponDao.Coupon coupon : coupons) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setBackgroundResource(R.drawable.bg_card);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(padding, padding, padding, padding);

            TextView itemView = new TextView(this);
            itemView.setTextColor(getResources().getColor(R.color.primary_text));
            itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            itemView.setText(coupon.name);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f);
            itemLayout.addView(itemView, nameParams);

            TextView actionView = new TextView(this);
            actionView.setText(R.string.cart_coupon_action);
            actionView.setTextColor(getResources().getColor(R.color.card_background));
            actionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            actionView.setBackgroundResource(R.drawable.bg_primary_button);
            int actionPaddingHorizontal = dpToPx(12);
            int actionPaddingVertical = dpToPx(6);
            actionView.setPadding(actionPaddingHorizontal, actionPaddingVertical,
                    actionPaddingHorizontal, actionPaddingVertical);
            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(CouponCenterActivity.this, CartActivity.class));
                }
            });
            LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            actionParams.leftMargin = dpToPx(8);
            itemLayout.addView(actionView, actionParams);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = margin;
            couponListLayout.addView(itemLayout, params);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
