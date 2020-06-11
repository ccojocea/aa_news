package com.ccojocea.aanews.data.remote;

public enum NewsCategory {
    BUSINESS("business"),
    ENTERTAINMENT("entertainment"),
    GENERAL("general"),
    HEALTH("health"),
    SCIENCE("science"),
    SPORTS("sports"),
    TECHNOLOGY("technology");

    private String value;

    NewsCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
