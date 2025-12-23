package com.example.cn.helloworld.data.cart;

public class FoodItem {
    private final String name;
    private final double price;
    private final String description;
    private final int imageResId;

    public FoodItem(String name, double price, String description, int imageResId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
