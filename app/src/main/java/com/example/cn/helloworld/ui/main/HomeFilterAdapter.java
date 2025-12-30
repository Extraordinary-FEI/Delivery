package com.example.cn.helloworld.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;

import java.util.List;

public class HomeFilterAdapter extends RecyclerView.Adapter<HomeFilterAdapter.FilterViewHolder> {

    interface OnFilterClickListener {
        void onFilterClick(String category);
    }

    private final List<String> categories;
    private final OnFilterClickListener listener;
    private String selectedCategory;

    public HomeFilterAdapter(List<String> categories, OnFilterClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FilterViewHolder holder, int position) {
        final String category = categories.get(position);
        boolean isSelected = category != null && category.equals(selectedCategory);
        holder.textView.setText(category);
        holder.textView.setTextColor(holder.itemView.getContext().getResources().getColor(
                isSelected ? R.color.primary_color : R.color.secondary_text));
        holder.indicatorView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(
                isSelected ? R.color.primary_color : android.R.color.transparent));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCategory = category;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onFilterClick(category);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final View indicatorView;

        FilterViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_home_filter);
            indicatorView = itemView.findViewById(R.id.view_home_filter_indicator);
        }
    }
}
