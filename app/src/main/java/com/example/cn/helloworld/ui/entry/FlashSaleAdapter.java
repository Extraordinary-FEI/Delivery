package com.example.cn.helloworld.ui.entry;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.model.SeckillItem;
import com.example.cn.helloworld.model.Food;

import java.util.List;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.SeckillViewHolder> {
    public interface OnSeckillActionListener {
        void onSeckillClick(SeckillItem item);

        void onSeckillBuy(SeckillItem item);
    }

    private final List<SeckillItem> items;
    private final OnSeckillActionListener listener;

    public FlashSaleAdapter(List<SeckillItem> items, OnSeckillActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public SeckillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flash_sale, parent, false);
        return new SeckillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SeckillViewHolder holder, int position) {
        final SeckillItem item = items.get(position);
        Food food = item.food;
        String name = food == null ? item.productId : food.getName();
        String desc = food == null ? "" : food.getDescription();
        double price = food == null ? 0 : food.getPrice();

        holder.nameView.setText(name);
        holder.descView.setText(desc);
        holder.priceView.setText(holder.itemView.getContext()
                .getString(R.string.flash_sale_price_format, price));
        holder.seckillPriceView.setText(holder.itemView.getContext()
                .getString(R.string.flash_sale_seckill_price_format, item.seckillPrice));
        holder.stockView.setText(holder.itemView.getContext()
                .getString(R.string.flash_sale_stock_format, item.stock));
        holder.buyButton.setEnabled(item.stock > 0 && item.status == 1);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSeckillClick(item);
                }
            }
        });

        holder.buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSeckillBuy(item);
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
        final TextView descView;
        final TextView priceView;
        final TextView seckillPriceView;
        final TextView stockView;
        final Button buyButton;

        SeckillViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_flash_name);
            descView = (TextView) itemView.findViewById(R.id.text_flash_desc);
            priceView = (TextView) itemView.findViewById(R.id.text_flash_price);
            seckillPriceView = (TextView) itemView.findViewById(R.id.text_flash_seckill_price);
            stockView = (TextView) itemView.findViewById(R.id.text_flash_stock);
            buyButton = (Button) itemView.findViewById(R.id.button_flash_buy);
        }
    }
}

