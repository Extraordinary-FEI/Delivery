package com.example.cn.helloworld.data;

import com.example.cn.helloworld.data.model.CartItem;
import com.example.cn.helloworld.data.model.Food;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<CartItem>();

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void addItem(Food food) {
        for (CartItem item : items) {
            if (item.getFood().getId().equals(food.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        items.add(new CartItem(food, 1));
    }

    public void updateQuantity(Food food, int quantity) {
        Iterator<CartItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getFood().getId().equals(food.getId())) {
                if (quantity <= 0) {
                    iterator.remove();
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    public void removeItem(Food food) {
        updateQuantity(food, 0);
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getTotalCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public void clear() {
        items.clear();
    }
}
