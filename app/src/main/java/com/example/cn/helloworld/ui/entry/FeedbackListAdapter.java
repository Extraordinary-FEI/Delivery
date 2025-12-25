package com.example.cn.helloworld.ui.entry;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.FeedbackDao;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeedbackListAdapter extends RecyclerView.Adapter<FeedbackListAdapter.FeedbackViewHolder> {
    interface OnFeedbackClickListener {
        void onFeedbackClick(FeedbackDao.FeedbackItem item);
    }

    private final List<FeedbackDao.FeedbackItem> items;
    private final OnFeedbackClickListener listener;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public FeedbackListAdapter(List<FeedbackDao.FeedbackItem> items, OnFeedbackClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public FeedbackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback_progress, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedbackViewHolder holder, int position) {
        final FeedbackDao.FeedbackItem item = items.get(position);
        holder.titleView.setText(resolveType(holder.itemView, item.type));
        holder.statusView.setText(resolveStatus(holder.itemView, item.status));
        holder.timeView.setText(formatter.format(item.createdAt));
        holder.contentView.setText(item.content);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onFeedbackClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String resolveType(View view, String type) {
        if (FeedbackDao.TYPE_COMPLAINT.equals(type)) {
            return view.getContext().getString(R.string.feedback_type_complaint);
        }
        return view.getContext().getString(R.string.feedback_type_feedback);
    }

    private String resolveStatus(View view, int status) {
        if (status == FeedbackDao.STATUS_PROCESSING) {
            return view.getContext().getString(R.string.feedback_status_processing);
        }
        if (status == FeedbackDao.STATUS_DONE) {
            return view.getContext().getString(R.string.feedback_status_done);
        }
        return view.getContext().getString(R.string.feedback_status_pending);
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView;
        final TextView statusView;
        final TextView timeView;
        final TextView contentView;

        FeedbackViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.text_feedback_title);
            statusView = (TextView) itemView.findViewById(R.id.text_feedback_status);
            timeView = (TextView) itemView.findViewById(R.id.text_feedback_time);
            contentView = (TextView) itemView.findViewById(R.id.text_feedback_content);
        }
    }
}
