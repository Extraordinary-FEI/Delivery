package com.example.cn.helloworld.model;

public class Food {
    private final String id;
    private final String name;
    private final String shopId;
    private final String description;
    private final double price;

    public Food(String id, String name, String shopId, String description, double price) {
        this.id = id;
        this.name = name;
        this.shopId = shopId;
        this.description = description;
        this.price = price;
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
}
