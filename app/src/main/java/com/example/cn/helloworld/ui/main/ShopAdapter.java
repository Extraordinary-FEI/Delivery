package com.example.cn.helloworld.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.model.Shop;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    private final List<Shop> shops;
    private final OnShopClickListener listener;

    public ShopAdapter(List<Shop> shops, OnShopClickListener listener) {
        this.shops = shops;
        this.listener = listener;
    }

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {
        final Shop shop = shops.get(position);
        holder.nameView.setText(shop.getName());
        holder.addressView.setText(shop.getAddress());
        holder.ratingView.setText(String.format("%.1f", shop.getRating()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onShopClick(shop);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return shops == null ? 0 : shops.size();
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView addressView;
        TextView ratingView;

        ShopViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_shop_name);
            addressView = (TextView) itemView.findViewById(R.id.text_shop_address);
            ratingView = (TextView) itemView.findViewById(R.id.text_shop_rating);
        }
    }
}

