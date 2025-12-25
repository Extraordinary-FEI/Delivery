package com.example.cn.helloworld.ui.market;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private final List<String> categories;
    private final OnCategoryClickListener listener;
    private String selectedCategory;

    public CategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_market_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        final String category = categories.get(position);
        holder.categoryName.setText(category);
        holder.itemView.setSelected(category.equals(selectedCategory));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryName;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = (TextView) itemView.findViewById(R.id.text_market_category);
        }
    }
}
