package com.example.cn.helloworld.ui.cart;

import java.math.BigDecimal;

public class CartItem {

    private final String name;
    private final BigDecimal unitPrice;
    private int quantity;

    public CartItem(String name, BigDecimal unitPrice, int quantity) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

