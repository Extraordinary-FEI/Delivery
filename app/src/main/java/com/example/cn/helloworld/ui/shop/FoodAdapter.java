package com.example.cn.helloworld.ui.shop;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.data.cart.FoodItem;
import com.example.cn.helloworld.model.Food;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public interface OnFoodLongClickListener {
        void onFoodLongClick(Food food);
    }

    public interface OnQuantityChangeListener {
        void onQuantityChange(int totalCount);
    }

    private final List<Food> foods;
    private final OnFoodClickListener listener;
    private final OnFoodLongClickListener longClickListener;
    private final OnQuantityChangeListener quantityChangeListener;

    public FoodAdapter(List<Food> foods, OnFoodClickListener listener) {
        this(foods, listener, null, null);
    }

    public FoodAdapter(List<Food> foods, OnFoodClickListener listener, OnFoodLongClickListener longClickListener) {
        this(foods, listener, longClickListener, null);
    }

    public FoodAdapter(List<Food> foods, OnFoodClickListener listener, OnFoodLongClickListener longClickListener,
                       OnQuantityChangeListener quantityChangeListener) {
        this.foods = foods;
        this.listener = listener;
        this.longClickListener = longClickListener;
        this.quantityChangeListener = quantityChangeListener;
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        final FoodViewHolder viewHolder = holder;
        final Food food = foods.get(position);
        viewHolder.nameView.setText(food.getName());
        viewHolder.descView.setText(food.getDescription());
        viewHolder.priceView.setText(viewHolder.itemView.getContext()
                .getString(R.string.food_price_format, food.getPrice()));
        final CartManager cartManager = CartManager.getInstance(viewHolder.itemView.getContext());
        int quantity = cartManager.getItemQuantity(food.getName());
        viewHolder.bindQuantity(quantity);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFoodClick(food);
                }
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onFoodLongClick(food);
                    return true;
                }
                return false;
            }
        });

        viewHolder.addSingleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartManager.addItem(new FoodItem(food.getName(), food.getPrice(), food.getDescription(), 0));
                int newQuantity = cartManager.getItemQuantity(food.getName());
                viewHolder.bindQuantity(newQuantity);
                animateCartAction(v);
                notifyQuantityChanged(cartManager);
            }
        });

        viewHolder.increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartManager.addItem(new FoodItem(food.getName(), food.getPrice(), food.getDescription(), 0));
                int newQuantity = cartManager.getItemQuantity(food.getName());
                viewHolder.bindQuantity(newQuantity);
                animateCartAction(v);
                notifyQuantityChanged(cartManager);
            }
        });

        viewHolder.decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = cartManager.getItemQuantity(food.getName());
                cartManager.updateItemQuantity(food.getName(), current - 1);
                int newQuantity = cartManager.getItemQuantity(food.getName());
                viewHolder.bindQuantity(newQuantity);
                animateCartAction(v);
                notifyQuantityChanged(cartManager);
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
        final TextView addSingleButton;
        final View quantityControls;
        final TextView decreaseButton;
        final TextView increaseButton;
        final TextView quantityText;

        FoodViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.food_item_name);
            descView = (TextView) itemView.findViewById(R.id.food_item_desc);
            priceView = (TextView) itemView.findViewById(R.id.food_item_price);
            addSingleButton = (TextView) itemView.findViewById(R.id.button_add_single);
            quantityControls = itemView.findViewById(R.id.quantity_controls);
            decreaseButton = (TextView) itemView.findViewById(R.id.button_decrease);
            increaseButton = (TextView) itemView.findViewById(R.id.button_increase);
            quantityText = (TextView) itemView.findViewById(R.id.text_quantity);
        }

        void bindQuantity(int quantity) {
            if (quantity <= 0) {
                addSingleButton.setVisibility(View.VISIBLE);
                quantityControls.setVisibility(View.GONE);
            } else {
                addSingleButton.setVisibility(View.GONE);
                quantityControls.setVisibility(View.VISIBLE);
                quantityText.setText(String.valueOf(quantity));
            }
        }
    }

    private void notifyQuantityChanged(CartManager cartManager) {
        if (quantityChangeListener != null) {
            quantityChangeListener.onQuantityChange(cartManager.getTotalCount());
        }
    }

    private void animateCartAction(View view) {
        view.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(120)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start();
                    }
                })
                .start();
    }
}
