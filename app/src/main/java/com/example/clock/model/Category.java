package com.example.clock.model;

import java.util.ArrayList;

public class Category {
    public int id;
    public String name;
    public int orderNum;
    public ArrayList<Ingredient> ingredients = new ArrayList<>();

    public Category(int id, String name, int orderNum) {
        this.id = id;
        this.name = name;
        this.orderNum = orderNum;
    }
}


