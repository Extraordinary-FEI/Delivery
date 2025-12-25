package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

public class ServiceHelpActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_help);
        setupBackButton();

        TextView adminHint = (TextView) findViewById(R.id.text_service_admin);
        if (SessionManager.isAdmin(this)) {
            adminHint.setText(R.string.service_admin_hint);
        }

        findViewById(R.id.card_faq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new android.content.Intent(ServiceHelpActivity.this, FaqActivity.class));
            }
        });
        findViewById(R.id.card_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new android.content.Intent(ServiceHelpActivity.this, FeedbackSubmitActivity.class));
            }
        });
        findViewById(R.id.card_service_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ServiceHelpActivity.this, R.string.service_chat_hint, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
