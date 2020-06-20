package com.ccojocea.aanews.data.remote.enums;

/**
 * Used by TopHeadlines API
 * Not all available for any country
 */
public enum NewsCategory {
    ALL(null),
    BUSINESS("business"),
    ENTERTAINMENT("entertainment"),
    GENERAL("general"),
    HEALTH("health"),
    SCIENCE("science"),
    SPORTS("sports"),
    TECHNOLOGY("technology");

    private final String value;

    NewsCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
