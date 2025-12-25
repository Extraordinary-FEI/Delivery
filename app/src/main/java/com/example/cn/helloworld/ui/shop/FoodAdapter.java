package com.example.cn.helloworld.ui.shop;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.cart.CartManager;
import com.example.cn.helloworld.data.cart.FoodItem;
import com.example.cn.helloworld.db.UserContentDao;
import com.example.cn.helloworld.model.Food;
import com.example.cn.helloworld.utils.ImageLoader;
import com.example.cn.helloworld.utils.SessionManager;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private static final int MAX_QUANTITY = 99;
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
    private UserContentDao contentDao;
    private int userId;

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
        ensureContentDao(viewHolder.itemView);
        viewHolder.nameView.setText(food.getName());
        viewHolder.descView.setText(food.getDescription());
        viewHolder.priceView.setText(viewHolder.itemView.getContext()
                .getString(R.string.food_price_format, food.getPrice()));
        ImageLoader.load(viewHolder.itemView.getContext(), viewHolder.imageView, food.getImageUrl());
        final CartManager cartManager = CartManager.getInstance(viewHolder.itemView.getContext());
        int quantity = cartManager.getItemQuantity(food.getName());
        viewHolder.bindQuantity(quantity);
        viewHolder.bindFavorite(contentDao.isFavorite(userId, resolveFoodId(food)));
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
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                cartManager.addItem(new FoodItem(
                        food.getName(),
                        food.getPrice(),
                        food.getDescription(),
                        0,
                        food.getImageUrl()));
                int newQuantity = cartManager.getItemQuantity(food.getName());
                viewHolder.bindQuantity(newQuantity);
                animateCartAction(v);
                notifyQuantityChanged(cartManager);
            }
        });

        viewHolder.increaseButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                cartManager.addItem(new FoodItem(
                        food.getName(),
                        food.getPrice(),
                        food.getDescription(),
                        0,
                        food.getImageUrl()));
                int newQuantity = cartManager.getItemQuantity(food.getName());
                viewHolder.bindQuantity(newQuantity);
                animateCartAction(v);
                notifyQuantityChanged(cartManager);
            }
        });

        viewHolder.decreaseButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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

        viewHolder.quantityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityDialog(viewHolder, cartManager, food);
            }
        });

        viewHolder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentDao.toggleFavorite(userId, food);
                viewHolder.bindFavorite(contentDao.isFavorite(userId, resolveFoodId(food)));
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
        final TextView favoriteButton;
        final ImageView imageView;

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
            favoriteButton = (TextView) itemView.findViewById(R.id.button_favorite);
            imageView = (ImageView) itemView.findViewById(R.id.food_item_image);
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

        void bindFavorite(boolean isFavorite) {
            favoriteButton.setText(isFavorite ? "★" : "☆");
            favoriteButton.setContentDescription(isFavorite
                    ? itemView.getContext().getString(R.string.favorite_added)
                    : itemView.getContext().getString(R.string.favorite_add));
        }
    }

    private void notifyQuantityChanged(CartManager cartManager) {
        if (quantityChangeListener != null) {
            quantityChangeListener.onQuantityChange(cartManager.getTotalCount());
        }
    }

    private void ensureContentDao(View view) {
        if (contentDao != null) {
            return;
        }
        contentDao = new UserContentDao(view.getContext());
        userId = parseUserId(SessionManager.getUserId(view.getContext()));
    }

    private void showQuantityDialog(final FoodViewHolder holder, final CartManager cartManager, final Food food) {
        final android.widget.EditText input = new android.widget.EditText(holder.itemView.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        int current = cartManager.getItemQuantity(food.getName());
        input.setText(String.valueOf(current));
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle(R.string.cart_quantity_quick_edit)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String value = input.getText().toString().trim();
                        int quantity = parseQuantity(value);
                        if (quantity < 1) {
                            Toast.makeText(holder.itemView.getContext(),
                                    R.string.cart_quantity_min_hint, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (quantity > MAX_QUANTITY) {
                            Toast.makeText(holder.itemView.getContext(),
                                    R.string.cart_quantity_max_hint, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        cartManager.updateItemQuantity(food.getName(), quantity);
                        holder.bindQuantity(quantity);
                        notifyQuantityChanged(cartManager);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private String resolveFoodId(Food food) {
        if (food == null) {
            return "";
        }
        if (food.getId() != null && food.getId().trim().length() > 0) {
            return food.getId();
        }
        return food.getName();
    }

    private int parseUserId(String userIdText) {
        try {
            return Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parseQuantity(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void animateCartAction(final View view) {
        final View targetView = view;
        targetView.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(120)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        targetView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start();
                    }
                })
                .start();
    }
}
