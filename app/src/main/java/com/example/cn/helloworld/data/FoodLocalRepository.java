package com.example.cn.helloworld.data;

import android.content.Context;
import android.text.TextUtils;

import com.example.cn.helloworld.model.Food;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FoodLocalRepository {
    private static final String FOOD_FILE = "foods.json";
    private static final Type FOOD_LIST_TYPE = new TypeToken<List<Food>>() { } .getType();

    public List<Food> getFoods(Context context) throws IOException {
        String json = LocalJsonStore.readJson(context, FOOD_FILE);
        List<Food> foods = new Gson().fromJson(json, FOOD_LIST_TYPE);
        return foods == null ? new ArrayList<Food>() : foods;
    }

    public List<Food> getFoodsForShop(Context context, String shopId) throws IOException {
        List<Food> foods = getFoods(context);
        if (TextUtils.isEmpty(shopId)) {
            return foods;
        }
        List<Food> filtered = new ArrayList<Food>();
        for (Food food : foods) {
            if (shopId.equals(food.getShopId())) {
                filtered.add(food);
            }
        }
        return filtered;
    }

    public Food getFoodById(Context context, String foodId) throws IOException {
        List<Food> foods = getFoods(context);
        for (Food food : foods) {
            if (foodId.equals(food.getId())) {
                return food;
            }
        }
        return null;
    }

    public void addFood(Context context, Food food) throws IOException {
        List<Food> foods = getFoods(context);
        foods.add(food);
        writeFoods(context, foods);
    }

    public void updateFood(Context context, Food updated) throws IOException {
        List<Food> foods = getFoods(context);
        for (int i = 0; i < foods.size(); i++) {
            if (foods.get(i).getId().equals(updated.getId())) {
                foods.set(i, updated);
                writeFoods(context, foods);
                return;
            }
        }
        foods.add(updated);
        writeFoods(context, foods);
    }

    private void writeFoods(Context context, List<Food> foods) throws IOException {
        String json = new Gson().toJson(foods, FOOD_LIST_TYPE);
        LocalJsonStore.writeJson(context, FOOD_FILE, json);
    }
}
