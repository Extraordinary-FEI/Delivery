package com.example.cn.helloworld.ui.cart;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.utils.ImageLoader;

import java.math.BigDecimal;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final int MAX_QUANTITY = 99;

    interface CartActionListener {
        void onQuantityChanged(CartItem item);

        void onItemRemoved(int position, CartItem item);
    }

    private final List<CartItem> items;
    private final CartActionListener actionListener;

    public CartAdapter(List<CartItem> items, CartActionListener actionListener) {
        this.items = items;
        this.actionListener = actionListener;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CartViewHolder holder, int position) {
        final CartItem item = items.get(position);
        holder.nameView.setText(item.getName());
        holder.priceView.setText(holder.itemView.getContext().getString(
                R.string.cart_item_price_format,
                item.getUnitPrice().toPlainString()));
        ImageLoader.load(holder.itemView.getContext(), holder.imageView, item.getImageUrl());
        holder.quantityView.setText(String.valueOf(item.getQuantity()));
        updateLineTotal(holder, item);

        holder.quantityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityDialog(holder, item);
            }
        });
        holder.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = item.getQuantity() + 1;
                item.setQuantity(quantity);
                holder.quantityView.setText(String.valueOf(quantity));
                updateLineTotal(holder, item);
                if (actionListener != null) {
                    actionListener.onQuantityChanged(item);
                }
            }
        });

        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = item.getQuantity();
                if (quantity > 1) {
                    quantity -= 1;
                    item.setQuantity(quantity);
                    holder.quantityView.setText(String.valueOf(quantity));
                    updateLineTotal(holder, item);
                    if (actionListener != null) {
                        actionListener.onQuantityChanged(item);
                    }
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onItemRemoved(holder.getAdapterPosition(), item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void updateLineTotal(CartViewHolder holder, CartItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
        holder.totalView.setText(holder.itemView.getContext().getString(
                R.string.cart_item_total_format,
                lineTotal.toPlainString()));
    }

    private void showQuantityDialog(final CartViewHolder holder, final CartItem item) {
        final TextView quantityView = holder.quantityView;
        final android.widget.EditText input = new android.widget.EditText(holder.itemView.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.getQuantity()));
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
                        item.setQuantity(quantity);
                        quantityView.setText(String.valueOf(quantity));
                        updateLineTotal(holder, item);
                        if (actionListener != null) {
                            actionListener.onQuantityChanged(item);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int parseQuantity(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView priceView;
        private final TextView totalView;
        private final TextView quantityView;
        private final Button plusButton;
        private final Button minusButton;
        private final Button deleteButton;
        private final ImageView imageView;

        CartViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_item_name);
            priceView = (TextView) itemView.findViewById(R.id.text_item_price);
            totalView = (TextView) itemView.findViewById(R.id.text_item_total);
            quantityView = (TextView) itemView.findViewById(R.id.text_item_quantity);
            plusButton = (Button) itemView.findViewById(R.id.button_quantity_plus);
            minusButton = (Button) itemView.findViewById(R.id.button_quantity_minus);
            deleteButton = (Button) itemView.findViewById(R.id.button_delete_item);
            imageView = (ImageView) itemView.findViewById(R.id.image_cart_item);
        }
    }
}
