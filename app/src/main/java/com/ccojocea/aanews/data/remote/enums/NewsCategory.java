package com.ccojocea.aanews.data.remote.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Nullable
    public String getValue() {
        return value;
    }

    @NonNull
    public static NewsCategory fromValue(@Nullable String value) {
        if (value != null) {
            for (NewsCategory category : values()) {
                if (value.equals(category.value)) {
                    return category;
                }
            }
        }
        return ALL;
    }
}
