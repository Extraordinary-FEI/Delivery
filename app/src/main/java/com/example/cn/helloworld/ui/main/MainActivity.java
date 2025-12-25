package com.example.cn.helloworld.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.cart.CartActivity;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.ui.entry.CouponCenterActivity;
import com.example.cn.helloworld.ui.entry.FlashSaleActivity;
import com.example.cn.helloworld.ui.entry.MemberCenterActivity;
import com.example.cn.helloworld.ui.entry.ServiceHelpActivity;
import com.example.cn.helloworld.ui.discover.DiscoverActivity;
import com.example.cn.helloworld.ui.shop.ShopListActivity;
import com.example.cn.helloworld.ui.shop.admin.AdminDashboardActivity;
import com.example.cn.helloworld.utils.SessionManager;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button browseButton = (Button) findViewById(R.id.button_browse_shops);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShopListActivity.class);
                startActivity(intent);
            }
        });

        View cartButton = findViewById(R.id.button_open_cart);
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        View cartSummaryButton = findViewById(R.id.button_cart_summary);
        cartSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        View flashSaleEntry = findViewById(R.id.entry_flash_sale);
        flashSaleEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FlashSaleActivity.class));
            }
        });

        View memberEntry = findViewById(R.id.entry_member_center);
        memberEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MemberCenterActivity.class));
            }
        });

        View couponEntry = findViewById(R.id.entry_coupon_center);
        couponEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CouponCenterActivity.class));
            }
        });

        View serviceEntry = findViewById(R.id.entry_service_help);
        serviceEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ServiceHelpActivity.class));
            }
        });

        Button adminButton = (Button) findViewById(R.id.button_admin_dashboard);
        if (SessionManager.isAdmin(this)) {
            adminButton.setVisibility(View.VISIBLE);
        }
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
            }
        });

        final View smartDock = findViewById(R.id.smart_dock);
        final View floatingBall = findViewById(R.id.floating_ball);
        View dockHandle = findViewById(R.id.smart_dock_handle);

        dockHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDock(smartDock, floatingBall);
            }
        });

        floatingBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDock(smartDock, floatingBall);
            }
        });

        setupFloatingBallDrag(floatingBall);

        findViewById(R.id.dock_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDock(smartDock, floatingBall);
            }
        });

        findViewById(R.id.dock_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShopListActivity.class));
            }
        });

        findViewById(R.id.dock_market).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DiscoverActivity.class));
            }
        });

        findViewById(R.id.dock_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            }
        });

        findViewById(R.id.dock_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MemberCenterActivity.class));
            }
        });
    }

    private void showDock(final View smartDock, final View floatingBall) {
        if (smartDock.getVisibility() == View.VISIBLE) {
            return;
        }
        smartDock.setVisibility(View.VISIBLE);
        smartDock.post(new Runnable() {
            @Override
            public void run() {
                smartDock.setAlpha(0f);
                smartDock.setTranslationY(smartDock.getHeight());
                smartDock.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(220)
                        .start();
            }
        });

        floatingBall.animate()
                .alpha(0f)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(180)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        floatingBall.setVisibility(View.GONE);
                        floatingBall.setAlpha(1f);
                        floatingBall.setScaleX(1f);
                        floatingBall.setScaleY(1f);
                    }
                })
                .start();
    }

    private void hideDock(final View smartDock, final View floatingBall) {
        if (smartDock.getVisibility() != View.VISIBLE) {
            return;
        }
        smartDock.animate()
                .alpha(0f)
                .translationY(smartDock.getHeight())
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        smartDock.setVisibility(View.GONE);
                        floatingBall.setAlpha(0f);
                        floatingBall.setVisibility(View.VISIBLE);
                        floatingBall.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(180)
                                .start();
                    }
                })
                .start();
    }

    private void setupFloatingBallDrag(final View floatingBall) {
        floatingBall.setOnTouchListener(new View.OnTouchListener() {
            private float dX;
            private float dY;
            private boolean dragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewGroup parent = (ViewGroup) v.getParent();
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        dragging = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        dragging = true;
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        float maxX = parent.getWidth() - v.getWidth();
                        float maxY = parent.getHeight() - v.getHeight();
                        v.setX(clamp(newX, 0f, maxX));
                        v.setY(clamp(newY, 0f, maxY));
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragging) {
                            return v.performClick();
                        }
                        snapFloatingBall(v, parent);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void snapFloatingBall(View floatingBall, ViewGroup parent) {
        float centerX = floatingBall.getX() + floatingBall.getWidth() / 2f;
        float targetX = centerX > parent.getWidth() / 2f
                ? parent.getWidth() - floatingBall.getWidth() - dpToPx(16f)
                : dpToPx(16f);
        float targetY = clamp(floatingBall.getY(), dpToPx(16f),
                parent.getHeight() - floatingBall.getHeight() - dpToPx(32f));
        floatingBall.animate()
                .x(targetX)
                .y(targetY)
                .setDuration(180)
                .start();
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
