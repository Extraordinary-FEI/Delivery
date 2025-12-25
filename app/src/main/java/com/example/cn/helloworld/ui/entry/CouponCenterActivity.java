package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.Locale;

public class CouponCenterActivity extends BaseActivity {
    private static final String BIRTHDAY_COUPON_NAME = "生日优惠券";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_center);
        setupBackButton();

        TextView adminHint = (TextView) findViewById(R.id.text_coupon_admin);
        if (SessionManager.isAdmin(this)) {
            adminHint.setText(R.string.coupon_admin_hint);
        }

        int userId = parseUserId(SessionManager.getUserId(this));
        if (userId > 0) {
            maybeGrantBirthdayCoupon(userId);
        }
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

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
