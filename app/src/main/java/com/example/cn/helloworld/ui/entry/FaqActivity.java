package com.example.cn.helloworld.ui.entry;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class FaqActivity extends BaseActivity implements FaqAdapter.OnFaqClickListener {
    private final List<FaqAdapter.FaqItem> items = new ArrayList<FaqAdapter.FaqItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        setupBackButton();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_faq);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        seedFaqItems();
        recyclerView.setAdapter(new FaqAdapter(items, this));
    }

    private void seedFaqItems() {
        items.clear();
        items.add(new FaqAdapter.FaqItem(getString(R.string.faq_question_order),
                getString(R.string.faq_answer_order)));
        items.add(new FaqAdapter.FaqItem(getString(R.string.faq_question_cancel),
                getString(R.string.faq_answer_cancel)));
        items.add(new FaqAdapter.FaqItem(getString(R.string.faq_question_coupon),
                getString(R.string.faq_answer_coupon)));
        items.add(new FaqAdapter.FaqItem(getString(R.string.faq_question_points),
                getString(R.string.faq_answer_points)));
        items.add(new FaqAdapter.FaqItem(getString(R.string.faq_question_delivery),
                getString(R.string.faq_answer_delivery)));
        items.add(new FaqAdapter.FaqItem(getString(R.string.faq_question_refund),
                getString(R.string.faq_answer_refund)));
    }

    @Override
    public void onFaqClick(FaqAdapter.FaqItem item) {
        new AlertDialog.Builder(this)
                .setTitle(item.question)
                .setMessage(item.answer)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
