package com.example.cn.helloworld.ui.shop.admin;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.model.Food;

import java.util.List;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.AdminFoodViewHolder> {
    public interface OnFoodActionListener {
        void onEdit(Food food);
        void onDelete(Food food);
        void onChangeCategory(Food food);
    }

    private final List<Food> foods;
    private final OnFoodActionListener listener;

    public AdminFoodAdapter(List<Food> foods, OnFoodActionListener listener) {
        this.foods = foods;
        this.listener = listener;
    }

    @Override
    public AdminFoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_food, parent, false);
        return new AdminFoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AdminFoodViewHolder holder, int position) {
        final Food food = foods.get(position);
        holder.nameView.setText(food.getName());
        holder.descView.setText(food.getDescription());
        holder.shopView.setText(holder.itemView.getContext()
                .getString(R.string.food_shop_format, food.getShopId()));
        holder.priceView.setText(holder.itemView.getContext()
                .getString(R.string.food_price_format, food.getPrice()));
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEdit(food);
                }
            }
        });
        holder.changeCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChangeCategory(food);
                }
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(food);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foods == null ? 0 : foods.size();
    }

    static class AdminFoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView descView;
        private final TextView shopView;
        private final TextView priceView;
        private final TextView editButton;
        private final TextView changeCategoryButton;
        private final TextView deleteButton;

        AdminFoodViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_admin_food_name);
            descView = (TextView) itemView.findViewById(R.id.text_admin_food_desc);
            shopView = (TextView) itemView.findViewById(R.id.text_admin_food_shop);
            priceView = (TextView) itemView.findViewById(R.id.text_admin_food_price);
            editButton = (TextView) itemView.findViewById(R.id.button_admin_food_edit);
            changeCategoryButton = (TextView) itemView.findViewById(R.id.button_admin_food_change_category);
            deleteButton = (TextView) itemView.findViewById(R.id.button_admin_food_delete);
        }
    }
}
