package com.ccojocea.aanews.data.remote.enums;

public enum NewsSortBy {
    RELEVANCY("relevancy"),
    POPULARITY("popularity"),
    PUBLISHED_AT("publishedAt");

    private final String value;

    NewsSortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
