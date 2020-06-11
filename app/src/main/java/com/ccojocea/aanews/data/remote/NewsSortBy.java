package com.ccojocea.aanews.data.remote;

public enum NewsSortBy {
    RELEVANCY("relevancy"),
    POPULARITY("popularity"),
    PUBLISHED_AT("publishedAt");

    private String value;

    NewsSortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
