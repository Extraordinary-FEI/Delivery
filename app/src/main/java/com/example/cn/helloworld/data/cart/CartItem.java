package com.example.cn.helloworld.data.cart;

public class CartItem {
    private final String name;
    private final double price;
    private int quantity;

    public CartItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity(int delta) {
        this.quantity += delta;
    }

    public double getSubtotal() {
        return price * quantity;
    }
}
