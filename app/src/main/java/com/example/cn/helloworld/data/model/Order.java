package com.example.cn.helloworld.data.model;

import java.util.List;

public class Order {
    private final String orderId;
    private final String orderTime;
    private final List<CartItem> items;
    private final double totalPrice;

    public Order(String orderId, String orderTime, List<CartItem> items, double totalPrice) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}

