package com.example.cn.helloworld.model;

import java.io.Serializable;

public class Shop implements Serializable {
    private final int id;
    private final String name;
    private final String address;
    private final double rating;
    private final String description;
    private final String phone;
    private final String imageUrl;

    public Shop(int id, String name, String address, double rating, String description, String phone, String imageUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.description = description;
        this.phone = phone;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
