package com.example.cn.helloworld.ui.entry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.FeedbackDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AfterSaleActivity extends BaseActivity implements FeedbackListAdapter.OnFeedbackClickListener {
    private final List<FeedbackDao.FeedbackItem> feedbackItems = new ArrayList<FeedbackDao.FeedbackItem>();
    private FeedbackDao feedbackDao;
    private FeedbackListAdapter adapter;
    private TextView emptyView;
    private TextView placeholderView;
    private TextView tabRefundView;
    private TextView tabFeedbackView;
    private int userId;
    private boolean showFeedbackTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_sale);
        setupBackButton();

        userId = parseUserId(SessionManager.getUserId(this));
        feedbackDao = new FeedbackDao(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_feedback_progress);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackListAdapter(feedbackItems, this);
        recyclerView.setAdapter(adapter);

        emptyView = (TextView) findViewById(R.id.text_feedback_empty);
        placeholderView = (TextView) findViewById(R.id.text_refund_placeholder);
        tabRefundView = (TextView) findViewById(R.id.tab_refund);
        tabFeedbackView = (TextView) findViewById(R.id.tab_feedback);

        tabRefundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTab(false);
            }
        });
        tabFeedbackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTab(true);
            }
        });

        switchTab(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFeedback();
    }

    private void loadFeedback() {
        feedbackItems.clear();
        feedbackItems.addAll(feedbackDao.listFeedback(userId));
        adapter.notifyDataSetChanged();
        if (showFeedbackTab) {
            emptyView.setVisibility(feedbackItems.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void switchTab(boolean showFeedback) {
        int activeColor = getResources().getColor(R.color.primary_color);
        showFeedbackTab = showFeedback;
        if (showFeedback) {
            tabFeedbackView.setBackgroundResource(R.drawable.bg_primary_button);
            tabFeedbackView.setTextColor(getResources().getColor(android.R.color.white));
            tabRefundView.setBackgroundResource(R.drawable.bg_quantity_outline);
            tabRefundView.setTextColor(activeColor);
            placeholderView.setVisibility(View.GONE);
            emptyView.setVisibility(feedbackItems.isEmpty() ? View.VISIBLE : View.GONE);
            findViewById(R.id.recycler_feedback_progress).setVisibility(View.VISIBLE);
        } else {
            tabRefundView.setBackgroundResource(R.drawable.bg_primary_button);
            tabRefundView.setTextColor(getResources().getColor(android.R.color.white));
            tabFeedbackView.setBackgroundResource(R.drawable.bg_quantity_outline);
            tabFeedbackView.setTextColor(activeColor);
            placeholderView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            findViewById(R.id.recycler_feedback_progress).setVisibility(View.GONE);
        }
    }

    @Override
    public void onFeedbackClick(FeedbackDao.FeedbackItem item) {
        Intent intent = new Intent(this, FeedbackDetailActivity.class);
        intent.putExtra(FeedbackDetailActivity.EXTRA_FEEDBACK_ID, item.id);
        startActivity(intent);
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

