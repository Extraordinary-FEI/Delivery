package com.example.cn.helloworld.ui.entry;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.PointsDao;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PointsLogAdapter extends RecyclerView.Adapter<PointsLogAdapter.PointsLogViewHolder> {
    private final List<PointsDao.PointsLog> logs;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public PointsLogAdapter(List<PointsDao.PointsLog> logs) {
        this.logs = logs;
    }

    @Override
    public PointsLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_points_log, parent, false);
        return new PointsLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PointsLogViewHolder holder, int position) {
        PointsDao.PointsLog log = logs.get(position);
        holder.remarkView.setText(resolveRemark(holder.itemView, log));
        holder.timeView.setText(formatter.format(log.createdAt));
        holder.changeView.setText(formatChange(log.change));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    private String resolveRemark(View view, PointsDao.PointsLog log) {
        if (log.remark != null && log.remark.trim().length() > 0) {
            return log.remark;
        }
        if (PointsDao.TYPE_ORDER.equals(log.type)) {
            return view.getContext().getString(R.string.points_type_order);
        }
        if (PointsDao.TYPE_REVIEW.equals(log.type)) {
            return view.getContext().getString(R.string.points_type_review);
        }
        if (PointsDao.TYPE_SIGN.equals(log.type)) {
            return view.getContext().getString(R.string.points_type_sign);
        }
        if (PointsDao.TYPE_INVITE.equals(log.type)) {
            return view.getContext().getString(R.string.points_type_invite);
        }
        if (PointsDao.TYPE_REDEEM.equals(log.type)) {
            return view.getContext().getString(R.string.points_type_redeem);
        }
        return view.getContext().getString(R.string.points_type_default);
    }

    private String formatChange(int change) {
        return change > 0 ? "+" + change : String.valueOf(change);
    }

    static class PointsLogViewHolder extends RecyclerView.ViewHolder {
        final TextView remarkView;
        final TextView timeView;
        final TextView changeView;

        PointsLogViewHolder(View itemView) {
            super(itemView);
            remarkView = (TextView) itemView.findViewById(R.id.text_points_log_remark);
            timeView = (TextView) itemView.findViewById(R.id.text_points_log_time);
            changeView = (TextView) itemView.findViewById(R.id.text_points_log_change);
        }
    }
}
