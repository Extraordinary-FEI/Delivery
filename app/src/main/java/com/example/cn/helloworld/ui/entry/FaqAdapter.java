package com.example.cn.helloworld.ui.entry;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {
    interface OnFaqClickListener {
        void onFaqClick(FaqItem item);
    }

    private final List<FaqItem> items;
    private final OnFaqClickListener listener;

    public FaqAdapter(List<FaqItem> items, OnFaqClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public FaqViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FaqViewHolder holder, int position) {
        final FaqItem item = items.get(position);
        holder.questionView.setText(item.question);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onFaqClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FaqItem {
        final String question;
        final String answer;

        FaqItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        final TextView questionView;

        FaqViewHolder(View itemView) {
            super(itemView);
            questionView = (TextView) itemView.findViewById(R.id.text_faq_question);
        }
    }
}

