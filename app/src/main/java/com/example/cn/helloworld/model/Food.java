package com.example.cn.helloworld.model;

public class Food {
    private final String id;
    private final String name;
    private final String shopId;
    private final String description;
    private final double price;
    private final int imageResId;

    public Food(String id, String name, String shopId, String description, double price, int imageResId) {
        this.id = id;
        this.name = name;
        this.shopId = shopId;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShopId() {
        return shopId;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}
