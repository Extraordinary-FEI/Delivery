package com.example.cn.helloworld.ui.order;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.db.OrderDao;

import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {
    public interface OnOrderActionListener {
        void onOrderClick(OrderDao.OrderSummary order);
    }

    private final List<OrderDao.OrderSummary> orders;
    private final OrderDao orderDao;
    private final OnOrderActionListener listener;

    public OrderListAdapter(List<OrderDao.OrderSummary> orders, OrderDao orderDao,
                            OnOrderActionListener listener) {
        this.orders = orders;
        this.orderDao = orderDao;
        this.listener = listener;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_summary, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        final OrderDao.OrderSummary order = orders.get(position);
        holder.orderIdView.setText(holder.itemView.getContext()
                .getString(R.string.order_id_short_format, order.orderId));
        holder.statusView.setText(getStatusLabel(holder.itemView, order.status));
        holder.amountView.setText(holder.itemView.getContext()
                .getString(R.string.order_pay_amount_format, order.payAmount));

        List<OrderDao.OrderItemDetail> items = orderDao.getOrderItems(order.orderId);
        holder.itemsView.setText(buildItemSummary(holder.itemView, items));
        holder.actionButton.setText(getActionLabel(holder.itemView, order.status));

        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    private String buildItemSummary(View view, List<OrderDao.OrderItemDetail> items) {
        if (items == null || items.isEmpty()) {
            return view.getContext().getString(R.string.order_items_placeholder);
        }
        String first = items.get(0).productName;
        int total = 0;
        for (OrderDao.OrderItemDetail item : items) {
            total += item.quantity;
        }
        if (items.size() == 1) {
            return view.getContext().getString(R.string.order_items_single_format, first, total);
        }
        return view.getContext().getString(R.string.order_items_multi_format, first, total);
    }

    private String getStatusLabel(View view, int status) {
        switch (status) {
            case OrderDao.STATUS_PENDING_PAY:
                return view.getContext().getString(R.string.order_status_pending);
            case OrderDao.STATUS_PAID:
                return view.getContext().getString(R.string.order_status_paid);
            case OrderDao.STATUS_PACKING:
                return view.getContext().getString(R.string.order_status_packing);
            case OrderDao.STATUS_DELIVERING:
                return view.getContext().getString(R.string.order_status_delivering);
            case OrderDao.STATUS_DELIVERED:
                return view.getContext().getString(R.string.order_status_delivered);
            case OrderDao.STATUS_CANCELLED:
                return view.getContext().getString(R.string.order_status_cancelled);
            default:
                return view.getContext().getString(R.string.order_status_pending);
        }
    }

    private String getActionLabel(View view, int status) {
        switch (status) {
            case OrderDao.STATUS_PENDING_PAY:
                return view.getContext().getString(R.string.order_action_pay);
            case OrderDao.STATUS_DELIVERING:
                return view.getContext().getString(R.string.order_action_track);
            case OrderDao.STATUS_DELIVERED:
                return view.getContext().getString(R.string.order_action_review);
            default:
                return view.getContext().getString(R.string.order_action_view);
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final TextView orderIdView;
        final TextView statusView;
        final TextView itemsView;
        final TextView amountView;
        final Button actionButton;

        OrderViewHolder(View itemView) {
            super(itemView);
            orderIdView = (TextView) itemView.findViewById(R.id.text_order_id);
            statusView = (TextView) itemView.findViewById(R.id.text_order_status);
            itemsView = (TextView) itemView.findViewById(R.id.text_order_items);
            amountView = (TextView) itemView.findViewById(R.id.text_order_amount);
            actionButton = (Button) itemView.findViewById(R.id.button_order_action);
        }
    }
}

