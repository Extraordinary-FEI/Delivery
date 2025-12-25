package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.ImageLoader;
import com.example.cn.helloworld.utils.SessionManager;
import com.example.cn.helloworld.ui.entry.FavoritesActivity;
import com.example.cn.helloworld.ui.entry.HistoryActivity;
import com.example.cn.helloworld.ui.order.OrderListActivity;

public class MemberCenterActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_center);
        setupBackButton();

        findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MemberCenterActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.button_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MemberCenterActivity.this, ServiceHelpActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.entry_favorites).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MemberCenterActivity.this, FavoritesActivity.class));
            }
        });
        findViewById(R.id.entry_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MemberCenterActivity.this, HistoryActivity.class));
            }
        });
        findViewById(R.id.entry_coupon_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MemberCenterActivity.this, CouponCenterActivity.class));
            }
        });
        findViewById(R.id.entry_points_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MemberCenterActivity.this, PointsCenterActivity.class));
            }
        });
        findViewById(R.id.entry_points_quick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MemberCenterActivity.this, PointsCenterActivity.class));
            }
        });
        findViewById(R.id.layout_profile_summary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MemberCenterActivity.this, SettingsActivity.class));
            }
        });
        findViewById(R.id.image_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MemberCenterActivity.this, SettingsActivity.class));
            }
        });
        findViewById(R.id.text_all_orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrderList(OrderListActivity.FILTER_ALL);
            }
        });
        findViewById(R.id.entry_order_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrderList(OrderListActivity.FILTER_ALL);
            }
        });
        findViewById(R.id.entry_order_pending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrderList(OrderListActivity.FILTER_PENDING);
            }
        });
        findViewById(R.id.entry_order_review).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrderList(OrderListActivity.FILTER_COMPLETED);
            }
        });
        findViewById(R.id.entry_order_refund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrderList(OrderListActivity.FILTER_ALL);
            }
        });

        bindProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindProfile();
    }

    private void bindProfile() {
        String userIdText = SessionManager.getUserId(this);
        int userId;
        try {
            userId = Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            userId = -1;
        }
        UserDao userDao = new UserDao(this);
        UserDao.UserProfile profile = userDao.getProfile(userId);
        if (profile == null) {
            return;
        }
        TextView usernameView = (TextView) findViewById(R.id.text_username);
        TextView phoneView = (TextView) findViewById(R.id.text_phone_summary);
        ImageView avatarView = (ImageView) findViewById(R.id.image_avatar);
        String displayName = profile.nickname == null || profile.nickname.trim().isEmpty()
                ? profile.username
                : profile.nickname;
        usernameView.setText(displayName);
        phoneView.setText(maskPhone(profile.phone));
        ImageLoader.loadAvatar(this, avatarView, profile.avatarUrl);

        TextView levelBadge = (TextView) findViewById(R.id.text_member_level_badge);
        TextView levelTitle = (TextView) findViewById(R.id.text_member_level_title);
        TextView growthView = (TextView) findViewById(R.id.text_member_growth);

        int points = profile.points;
        MemberLevel level = MemberLevel.resolve(points);

        levelBadge.setText(level.title);
        levelTitle.setText(level.title);
        growthView.setText(getString(R.string.member_profile_growth_format, points, level.nextTarget));

        TextView pointsView = (TextView) findViewById(R.id.text_points_value);
        TextView couponView = (TextView) findViewById(R.id.text_coupon_value);
        TextView balanceView = (TextView) findViewById(R.id.text_balance_value);
        pointsView.setText(String.valueOf(points));
        couponView.setText(getString(R.string.member_coupon_value));
        balanceView.setText(getString(R.string.member_balance_value));
    }

    private static class MemberLevel {
        final String title;
        final int nextTarget;

        private MemberLevel(String title, int nextTarget) {
            this.title = title;
            this.nextTarget = nextTarget;
        }

        static MemberLevel resolve(int points) {
            if (points < 200) {
                return new MemberLevel("新芽会员", 200);
            }
            if (points < 600) {
                return new MemberLevel("星耀会员", 600);
            }
            if (points < 1200) {
                return new MemberLevel("银翼会员", 1200);
            }
            if (points < 2000) {
                return new MemberLevel("金穗会员", 2000);
            }
            return new MemberLevel("钻石会员", 3000);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return getString(R.string.member_profile_phone_unbound);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private void openOrderList(int filter) {
        Intent intent = new Intent(this, OrderListActivity.class);
        intent.putExtra(OrderListActivity.EXTRA_FILTER, filter);
        startActivity(intent);
    }
}
