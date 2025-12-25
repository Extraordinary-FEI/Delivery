package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.CouponDao;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

public class PointsCenterActivity extends BaseActivity {
    private TextView pointsView;
    private UserDao userDao;
    private CouponDao couponDao;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_center);
        setupBackButton();

        userId = parseUserId(SessionManager.getUserId(this));
        userDao = new UserDao(this);
        couponDao = new CouponDao(this);
        pointsView = (TextView) findViewById(R.id.text_points_value);

        findViewById(R.id.button_exchange_coupon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeCoupon("5元外卖券", 500);
            }
        });
        findViewById(R.id.button_exchange_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeCoupon("免配送费券", 300);
            }
        });
        findViewById(R.id.button_exchange_member).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeCoupon("会员日礼包", 800);
            }
        });
        refreshPoints();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPoints();
    }

    private void refreshPoints() {
        int points = userDao.getPoints(userId);
        pointsView.setText(String.valueOf(points));
    }

    private void exchangeCoupon(String name, int cost) {
        int points = userDao.getPoints(userId);
        if (points < cost) {
            Toast.makeText(this, R.string.points_exchange_insufficient, Toast.LENGTH_SHORT).show();
            return;
        }
        userDao.deductPoints(userId, cost);
        couponDao.insertCoupon(userId, name, cost);
        Toast.makeText(this, R.string.points_exchange_success, Toast.LENGTH_SHORT).show();
        refreshPoints();
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
