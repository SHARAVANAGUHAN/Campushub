package com.example.campushub.activities;

public class Book {
    private String id;
    private String title;
    private String coverUrl;
    private int progressPercentage;
    private boolean isLocked;

    public Book(String id, String title, String coverUrl, int progressPercentage, boolean isLocked) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.progressPercentage = progressPercentage;
        this.isLocked = isLocked;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return coverUrl; }
    public int getProgressPercentage() { return progressPercentage; }
    public boolean isLocked() { return isLocked; }
}