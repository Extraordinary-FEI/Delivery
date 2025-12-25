package com.example.cn.helloworld.data.model;

import com.example.cn.helloworld.model.Food;

public class SeckillItem {
    public final long id;
    public final String productId;
    public final double seckillPrice;
    public final int stock;
    public final long startTime;
    public final long endTime;
    public final int status;
    public final Food food;

    public SeckillItem(long id, String productId, double seckillPrice, int stock,
                       long startTime, long endTime, int status, Food food) {
        this.id = id;
        this.productId = productId;
        this.seckillPrice = seckillPrice;
        this.stock = stock;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.food = food;
    }
}
