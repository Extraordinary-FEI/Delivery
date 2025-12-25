package com.example.cn.helloworld.ui.entry;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder> {
    public interface OnResultClickListener {
        void onResultClick(SearchResultActivity.SearchResultItem item);
    }

    private final List<SearchResultActivity.SearchResultItem> items;
    private final OnResultClickListener listener;

    public SearchResultAdapter(List<SearchResultActivity.SearchResultItem> items,
                               OnResultClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        final SearchResultActivity.SearchResultItem item = items.get(position);
        holder.titleView.setText(item.title);
        holder.typeView.setText(resolveTypeLabel(item.type));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onResultClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    private String resolveTypeLabel(String type) {
        if ("product".equals(type)) {
            return "商品";
        }
        if ("shop".equals(type)) {
            return "店铺";
        }
        return "服务";
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView;
        final TextView typeView;

        ResultViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.text_search_title);
            typeView = (TextView) itemView.findViewById(R.id.text_search_type);
        }
    }
}
