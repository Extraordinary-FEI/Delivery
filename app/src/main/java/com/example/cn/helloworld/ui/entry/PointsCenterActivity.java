package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.CouponDao;
import com.example.cn.helloworld.db.PointsDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.widget.PointsCurveView;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PointsCenterActivity extends BaseActivity {
    private TextView pointsView;
    private TextView pointsGapView;
    private TextView pointsLevelView;
    private PointsDao pointsDao;
    private CouponDao couponDao;
    private int userId;
    private PointsCurveView curveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_center);
        setupBackButton();

        userId = parseUserId(SessionManager.getUserId(this));
        pointsDao = new PointsDao(this);
        couponDao = new CouponDao(this);
        pointsView = (TextView) findViewById(R.id.text_points_value);
        pointsGapView = (TextView) findViewById(R.id.text_points_gap);
        pointsLevelView = (TextView) findViewById(R.id.text_points_level);
        curveView = (PointsCurveView) findViewById(R.id.view_points_curve);

        findViewById(R.id.text_points_rule_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new android.content.Intent(PointsCenterActivity.this, PointsRuleActivity.class));
            }
        });
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
        int points = pointsDao.getPoints(userId);
        pointsView.setText(String.valueOf(points));
        bindLevel(points);
        bindCurve(points);
    }

    private void bindLevel(int points) {
        MemberLevel level = MemberLevel.resolve(points);
        if (pointsLevelView != null) {
            pointsLevelView.setText(getString(R.string.points_level_format, level.title));
        }
        if (pointsGapView != null) {
            int gap = Math.max(level.nextTarget - points, 0);
            pointsGapView.setText(getString(R.string.points_gap_format, gap));
        }
    }

    private void bindCurve(int currentPoints) {
        if (curveView == null) {
            return;
        }
        List<PointsDao.PointsLog> logs = pointsDao.listLogs(userId);
        int maxRecords = 6;
        int records = Math.min(maxRecords, logs.size());
        List<Integer> seriesDesc = new ArrayList<>();
        int runningPoints = currentPoints;
        seriesDesc.add(runningPoints);
        for (int i = 0; i < records; i++) {
            runningPoints = Math.max(0, runningPoints - logs.get(i).change);
            seriesDesc.add(runningPoints);
        }
        Collections.reverse(seriesDesc);
        curveView.setPoints(seriesDesc);
    }

    private void exchangeCoupon(String name, int cost) {
        int points = pointsDao.getPoints(userId);
        if (points < cost) {
            Toast.makeText(this, R.string.points_exchange_insufficient, Toast.LENGTH_SHORT).show();
            return;
        }
        pointsDao.deductPoints(userId, cost, PointsDao.TYPE_REDEEM, name,
                getString(R.string.points_log_redeem, name, cost));
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
