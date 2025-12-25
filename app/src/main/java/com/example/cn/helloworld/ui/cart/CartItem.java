package com.example.cn.helloworld.ui.cart;

import java.math.BigDecimal;

/**
 * UI model for cart items with BigDecimal prices to avoid floating point errors.
 */
public class CartItem {
    private final String name;
    private final BigDecimal unitPrice;
    private final String imageUrl;
    private int quantity;

    public CartItem(String name, BigDecimal unitPrice, int quantity, String imageUrl) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}
