package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.UserDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.main.MainActivity;
import com.example.cn.helloworld.ui.market.FoodMarketActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;
import com.example.cn.helloworld.utils.AvatarLoader;
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
        findViewById(R.id.layout_points_entry).setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.entry_order_summary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrderList(OrderListActivity.FILTER_ALL);
            }
        });
        findViewById(R.id.entry_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MemberCenterActivity.this, ServiceHelpActivity.class);
                startActivity(intent);
            }
        });

        setupDockActions();
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
        AvatarLoader.load(this, avatarView, profile.avatarUrl);

        TextView levelBadge = (TextView) findViewById(R.id.text_member_level_badge);
        TextView levelTitle = (TextView) findViewById(R.id.text_member_level_title);
        TextView growthView = (TextView) findViewById(R.id.text_member_growth);
        ProgressBar growthProgress = (ProgressBar) findViewById(R.id.progress_member_growth);

        int points = profile.points;
        MemberLevel level = MemberLevel.resolve(points);

        levelBadge.setText(level.displayTitle);
        levelTitle.setText(level.title);
        growthView.setText(getString(R.string.member_profile_growth_format, points, level.nextTarget));
        if (growthProgress != null) {
            int target = Math.max(level.nextTarget, 1);
            int progress = Math.min(100, Math.max(0, points * 100 / target));
            growthProgress.setProgress(progress);
        }

        TextView balanceView = (TextView) findViewById(R.id.text_balance_value);
        balanceView.setText(getString(R.string.member_balance_value));
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

    private void setupDockActions() {
        findViewById(R.id.dock_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MemberCenterActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.dock_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MemberCenterActivity.this, ShopListActivity.class));
            }
        });

        findViewById(R.id.dock_market).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MemberCenterActivity.this, FoodMarketActivity.class));
            }
        });

        findViewById(R.id.dock_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MemberCenterActivity.this, CartActivity.class));
            }
        });

        findViewById(R.id.dock_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on profile.
            }
        });
    }
}
