package com.example.cn.helloworld.data.cart;

public class CartItem {
    private final String name;
    private final double price;
    private final String imageUrl;
    private int quantity;

    public CartItem(String name, double price, int quantity, String imageUrl) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity(int delta) {
        this.quantity += delta;
    }

    public double getSubtotal() {
        return price * quantity;
    }
}
