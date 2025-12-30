package com.example.cn.helloworld.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.model.Shop;
import com.example.cn.helloworld.utils.ImageLoader;

import java.util.List;
import java.util.Locale;

public class RecommendedShopAdapter extends RecyclerView.Adapter<RecommendedShopAdapter.RecommendedShopViewHolder> {
    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    private final List<Shop> shops;
    private final OnShopClickListener listener;

    public RecommendedShopAdapter(List<Shop> shops, OnShopClickListener listener) {
        this.shops = shops;
        this.listener = listener;
    }

    @Override
    public RecommendedShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommended_shop, parent, false);
        return new RecommendedShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecommendedShopViewHolder holder, int position) {
        final Shop shop = shops.get(position);
        holder.nameView.setText(shop.getName());
        holder.categoryView.setText(shop.getDescription());
        holder.metaView.setText(shop.getAddress());
        holder.ratingView.setText(String.format(Locale.getDefault(), "★ %.1f", shop.getRating()));
        ImageLoader.load(holder.itemView.getContext(), holder.imageView, shop.getImageUrl());
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

    static class RecommendedShopViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView ratingView;
        private final TextView categoryView;
        private final TextView metaView;
        private final ImageView imageView;

        RecommendedShopViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_recommend_shop_name);
            ratingView = (TextView) itemView.findViewById(R.id.text_recommend_shop_rating);
            categoryView = (TextView) itemView.findViewById(R.id.text_recommend_shop_category);
            metaView = (TextView) itemView.findViewById(R.id.text_recommend_shop_meta);
            imageView = (ImageView) itemView.findViewById(R.id.image_recommend_shop);
        }
    }
}
