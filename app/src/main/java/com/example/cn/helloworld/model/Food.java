package com.example.cn.helloworld.model;

public class Food {
    private final String id;
    private final String name;
    private final String shopId;
    private final String description;
    private final double price;
    private final String imageUrl;
    private final String category;

    public Food(String id, String name, String shopId, String description, double price, String imageUrl) {
        this(id, name, shopId, description, price, imageUrl, null);
    }

    public Food(String id, String name, String shopId, String description, double price, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.shopId = shopId;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }
}
