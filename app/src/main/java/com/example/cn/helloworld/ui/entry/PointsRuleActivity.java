package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.PointsDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PointsRuleActivity extends BaseActivity {
    private final List<PointsDao.PointsLog> logs = new ArrayList<PointsDao.PointsLog>();
    private PointsLogAdapter adapter;
    private PointsDao pointsDao;
    private int userId;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_rule);
        setupBackButton();

        userId = parseUserId(SessionManager.getUserId(this));
        pointsDao = new PointsDao(this);
        emptyView = (TextView) findViewById(R.id.text_points_log_empty);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_points_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PointsLogAdapter(logs);
        recyclerView.setAdapter(adapter);
        loadLogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLogs();
    }

    private void loadLogs() {
        logs.clear();
        logs.addAll(pointsDao.listLogs(userId));
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

