package com.example.cn.helloworld.ui.shop;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.model.Shop;

import java.util.List;
import java.util.Locale;

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
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {
        final Shop shop = shops.get(position);
        holder.nameView.setText(shop.getName());
        holder.addressView.setText(shop.getAddress());
        holder.ratingView.setText(String.format(Locale.getDefault(), "%.1f", shop.getRating()));
        holder.descriptionView.setText(shop.getDescription());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        private final TextView nameView;
        private final TextView addressView;
        private final TextView ratingView;
        private final TextView descriptionView;

        ShopViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_shop_name);
            addressView = (TextView) itemView.findViewById(R.id.text_shop_address);
            ratingView = (TextView) itemView.findViewById(R.id.text_shop_rating);
            descriptionView = (TextView) itemView.findViewById(R.id.text_shop_description);
        }
    }
}
