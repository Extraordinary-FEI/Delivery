package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.FeedbackDao;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FeedbackDetailActivity extends BaseActivity {
    public static final String EXTRA_FEEDBACK_ID = "extra_feedback_id";

    private FeedbackDao feedbackDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_detail);
        setupBackButton();

        feedbackDao = new FeedbackDao(this);
        int feedbackId = getIntent().getIntExtra(EXTRA_FEEDBACK_ID, -1);
        bindDetail(feedbackId);
    }

    private void bindDetail(int feedbackId) {
        FeedbackDao.FeedbackItem item = feedbackDao.getFeedbackById(feedbackId);
        if (item == null) {
            return;
        }
        TextView titleView = (TextView) findViewById(R.id.text_feedback_detail_title);
        TextView statusView = (TextView) findViewById(R.id.text_feedback_detail_status);
        TextView timeView = (TextView) findViewById(R.id.text_feedback_detail_time);
        TextView contentView = (TextView) findViewById(R.id.text_feedback_detail_content);
        TextView replyView = (TextView) findViewById(R.id.text_feedback_detail_reply);

        titleView.setText(resolveType(item.type));
        statusView.setText(resolveStatus(item.status));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        timeView.setText(formatter.format(item.createdAt));
        contentView.setText(item.content);
        replyView.setText(item.reply == null || item.reply.trim().isEmpty()
                ? getString(R.string.feedback_reply_placeholder)
                : item.reply);
    }

    private String resolveType(String type) {
        if (FeedbackDao.TYPE_COMPLAINT.equals(type)) {
            return getString(R.string.feedback_type_complaint);
        }
        return getString(R.string.feedback_type_feedback);
    }

    private String resolveStatus(int status) {
        if (status == FeedbackDao.STATUS_PROCESSING) {
            return getString(R.string.feedback_status_processing);
        }
        if (status == FeedbackDao.STATUS_DONE) {
            return getString(R.string.feedback_status_done);
        }
        return getString(R.string.feedback_status_pending);
    }
}
