package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.CouponDao;
import com.example.cn.helloworld.db.UserDao;
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
            TextView itemView = new TextView(this);
            itemView.setBackgroundResource(R.drawable.bg_card);
            itemView.setTextColor(getResources().getColor(R.color.primary_text));
            itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            itemView.setPadding(padding, padding, padding, padding);
            itemView.setText(coupon.name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = margin;
            couponListLayout.addView(itemView, params);
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
