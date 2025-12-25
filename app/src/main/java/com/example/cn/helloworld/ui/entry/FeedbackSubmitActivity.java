package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.FeedbackDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

public class FeedbackSubmitActivity extends BaseActivity {
    private FeedbackDao feedbackDao;
    private int userId;
    private EditText contentInput;
    private RadioGroup typeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_submit);
        setupBackButton();

        userId = parseUserId(SessionManager.getUserId(this));
        feedbackDao = new FeedbackDao(this);
        contentInput = (EditText) findViewById(R.id.input_feedback_content);
        typeGroup = (RadioGroup) findViewById(R.id.group_feedback_type);

        findViewById(R.id.button_submit_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitFeedback();
            }
        });
    }

    private void submitFeedback() {
        String content = contentInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, R.string.feedback_content_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String type = typeGroup.getCheckedRadioButtonId() == R.id.radio_feedback_complaint
                ? FeedbackDao.TYPE_COMPLAINT
                : FeedbackDao.TYPE_FEEDBACK;
        feedbackDao.insertFeedback(userId, type, content);
        Toast.makeText(this, R.string.feedback_submit_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

