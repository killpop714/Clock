package com.example.clock.model;

public class CookingStep {
    public int id;
    public int recipeId;
    public int step;
    public String title;
    public String description;
    public String imageUrl;

    public CookingStep(int id, int recipeId, int step, String title, String description, String imageUrl) {
        this.id = id;
        this.recipeId = recipeId;
        this.step = step;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}

