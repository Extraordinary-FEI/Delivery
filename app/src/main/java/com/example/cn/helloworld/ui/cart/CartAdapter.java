package com.example.cn.helloworld.ui.cart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cn.helloworld.R;

import java.math.BigDecimal;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    interface CartActionListener {
        void onQuantityChanged();

        void onItemRemoved(int position);
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
        holder.quantityView.setText(String.valueOf(item.getQuantity()));
        updateLineTotal(holder, item);

        holder.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = item.getQuantity() + 1;
                item.setQuantity(quantity);
                holder.quantityView.setText(String.valueOf(quantity));
                updateLineTotal(holder, item);
                if (actionListener != null) {
                    actionListener.onQuantityChanged();
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
                        actionListener.onQuantityChanged();
                    }
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onItemRemoved(holder.getAdapterPosition());
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

    static class CartViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView priceView;
        private final TextView totalView;
        private final TextView quantityView;
        private final Button plusButton;
        private final Button minusButton;
        private final Button deleteButton;

        CartViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_item_name);
            priceView = (TextView) itemView.findViewById(R.id.text_item_price);
            totalView = (TextView) itemView.findViewById(R.id.text_item_total);
            quantityView = (TextView) itemView.findViewById(R.id.text_item_quantity);
            plusButton = (Button) itemView.findViewById(R.id.button_quantity_plus);
            minusButton = (Button) itemView.findViewById(R.id.button_quantity_minus);
            deleteButton = (Button) itemView.findViewById(R.id.button_delete_item);
        }
    }
}
