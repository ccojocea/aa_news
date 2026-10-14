package com.ccojocea.aanews.data.remote.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Used by the Everything API
 */
public enum NewsSortBy {
    PUBLISHED_AT("publishedAt"),
    RELEVANCY("relevancy"),
    POPULARITY("popularity");

    public static final NewsSortBy DEFAULT = PUBLISHED_AT;

    private final String value;

    NewsSortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @NonNull
    public static NewsSortBy fromValue(@Nullable String value) {
        for (NewsSortBy sortBy : values()) {
            if (sortBy.value.equals(value)) {
                return sortBy;
            }
        }
        return DEFAULT;
    }
}
