package com.example.campushub.activities;

import android.net.Uri;

public class Book {
    private final String id;
    private final String title;
    private final String coverUrl;
    private final String documentUri;
    private int progressPercentage;
    private final boolean isLocked;

    public Book(String id, String title, String coverUrl, int progressPercentage, boolean isLocked) {
        this(id, title, coverUrl, null, progressPercentage, isLocked);
    }

    public Book(String id, String title, String coverUrl, String documentUri,
                int progressPercentage, boolean isLocked) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.documentUri = documentUri;
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
        this.isLocked = isLocked;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return coverUrl; }
    public String getDocumentUri() { return documentUri; }
    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
    }
    public boolean isLocked() { return isLocked; }
    public Uri getDocumentUriValue() {
        return documentUri == null || documentUri.isEmpty() ? null : Uri.parse(documentUri);
    }
}
