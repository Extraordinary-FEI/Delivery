package com.example.cn.helloworld.ui.shop;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.model.Food;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public interface OnFoodLongClickListener {
        void onFoodLongClick(Food food);
    }

    private final List<Food> foods;
    private final OnFoodClickListener listener;
    private final OnFoodLongClickListener longClickListener;

    public FoodAdapter(List<Food> foods, OnFoodClickListener listener) {
        this(foods, listener, null);
    }

    public FoodAdapter(List<Food> foods, OnFoodClickListener listener, OnFoodLongClickListener longClickListener) {
        this.foods = foods;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        final Food food = foods.get(position);
        holder.nameView.setText(food.getName());
        holder.descView.setText(food.getDescription());
        holder.priceView.setText(holder.itemView.getContext()
                .getString(R.string.food_price_format, food.getPrice()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFoodClick(food);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onFoodLongClick(food);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return foods == null ? 0 : foods.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView descView;
        final TextView priceView;

        FoodViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.food_item_name);
            descView = (TextView) itemView.findViewById(R.id.food_item_desc);
            priceView = (TextView) itemView.findViewById(R.id.food_item_price);
        }
    }
}
