package com.example.cn.helloworld.ui.order;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.ReviewDao;
import com.example.cn.helloworld.db.PointsDao;
import com.example.cn.helloworld.ui.common.BaseActivity;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.ArrayList;

public class ReviewActivity extends BaseActivity {
    public static final String EXTRA_ITEMS = "extra_items";
    public static final String EXTRA_FOOD_ID = "extra_food_id";
    public static final String EXTRA_FOOD_NAME = "extra_food_name";

    private Spinner foodSpinner;
    private EditText reviewInput;
    private ReviewDao reviewDao;
    private PointsDao pointsDao;
    private int userId;
    private ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        setupBackButton();

        userId = parseUserId(SessionManager.getUserId(this));
        reviewDao = new ReviewDao(this);
        pointsDao = new PointsDao(this);

        foodSpinner = (Spinner) findViewById(R.id.spinner_review_food);
        reviewInput = (EditText) findViewById(R.id.input_review_content);
        Button submitButton = (Button) findViewById(R.id.button_submit_review);

        items = getIntent().getStringArrayListExtra(EXTRA_ITEMS);
        String foodId = getIntent().getStringExtra(EXTRA_FOOD_ID);
        String foodName = getIntent().getStringExtra(EXTRA_FOOD_NAME);
        if (items == null || items.isEmpty()) {
            items = new ArrayList<String>();
            if (!TextUtils.isEmpty(foodName)) {
                items.add(foodName);
            } else if (!TextUtils.isEmpty(foodId)) {
                items.add(foodId);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodSpinner.setAdapter(adapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });
    }

    private void submitReview() {
        String content = reviewInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, R.string.review_content_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String selected = (String) foodSpinner.getSelectedItem();
        if (TextUtils.isEmpty(selected)) {
            Toast.makeText(this, R.string.review_content_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        reviewDao.addReview(userId, selected, selected, content);
        pointsDao.addPoints(userId, 30, PointsDao.TYPE_REVIEW, selected,
                getString(R.string.points_log_review));
        Toast.makeText(this, R.string.review_submit_success, Toast.LENGTH_SHORT).show();
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

