package com.example.clock.model;

public class Recipe {
    public int id;
    public String title;
    public String description;
    public String imageUrl;
    public int point;

    public Recipe(int id, String title, String description, String imageUrl, int point) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.point = point;
    }

    public String getTitle() {
        return title;
    }
}



