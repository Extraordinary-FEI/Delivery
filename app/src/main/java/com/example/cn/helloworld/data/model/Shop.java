package com.example.cn.helloworld.data.model;

import java.util.List;

public class Shop {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String hours;
    private double rating;
    private String description;
    private String imageUrl;
    private List<Food> foods;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getHours() {
        return hours;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<Food> getFoods() {
        return foods;
    }
}
