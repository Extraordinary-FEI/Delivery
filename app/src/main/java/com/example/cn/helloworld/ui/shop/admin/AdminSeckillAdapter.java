package com.example.cn.helloworld.ui.shop.admin;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.model.SeckillItem;
import com.example.cn.helloworld.model.Food;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminSeckillAdapter extends RecyclerView.Adapter<AdminSeckillAdapter.SeckillViewHolder> {
    public interface OnSeckillAdminActionListener {
        void onEdit(SeckillItem item);

        void onToggle(SeckillItem item);

        void onDelete(SeckillItem item);
    }

    private final List<SeckillItem> items;
    private final OnSeckillAdminActionListener listener;

    public AdminSeckillAdapter(List<SeckillItem> items, OnSeckillAdminActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public SeckillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_seckill, parent, false);
        return new SeckillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SeckillViewHolder holder, int position) {
        final SeckillItem item = items.get(position);
        Food food = item.food;
        String name = food == null ? item.productId : food.getName();
        String detail = holder.itemView.getContext().getString(
                R.string.admin_seckill_detail_format,
                item.seckillPrice,
                item.stock,
                formatTime(item.startTime),
                formatTime(item.endTime),
                item.status == 1 ? holder.itemView.getContext().getString(R.string.admin_seckill_status_on)
                        : holder.itemView.getContext().getString(R.string.admin_seckill_status_off)
        );
        holder.nameView.setText(name);
        holder.detailView.setText(detail);
        holder.toggleButton.setText(item.status == 1
                ? holder.itemView.getContext().getString(R.string.admin_seckill_toggle_off)
                : holder.itemView.getContext().getString(R.string.admin_seckill_toggle_on));

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEdit(item);
                }
            }
        });
        holder.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onToggle(item);
                }
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class SeckillViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView detailView;
        final Button editButton;
        final Button toggleButton;
        final Button deleteButton;

        SeckillViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_seckill_name);
            detailView = (TextView) itemView.findViewById(R.id.text_seckill_detail);
            editButton = (Button) itemView.findViewById(R.id.button_edit_seckill);
            toggleButton = (Button) itemView.findViewById(R.id.button_toggle_seckill);
            deleteButton = (Button) itemView.findViewById(R.id.button_delete_seckill);
        }
    }

    private String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        return format.format(new Date(time));
    }
}

