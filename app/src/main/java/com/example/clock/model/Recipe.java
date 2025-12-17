package com.example.clock.model;

public class Recipe {
    public int id;
    public String title;
    public String description;
    public String imageUrl;
    public int point;

    public String portion;       // 인분
    public String cookingTime;   // 요리 시간
    public String difficulty;    // 난이도

    public Recipe(int id, String title, String description, String imageUrl, int point, String portion, String cookingTime, String difficulty) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.point = point;
        this.portion = portion;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
    }

    public String getTitle() {
        return title;
    }
}



