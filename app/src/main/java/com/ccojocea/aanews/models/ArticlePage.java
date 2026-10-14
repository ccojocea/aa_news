package com.ccojocea.aanews.models;

import androidx.annotation.NonNull;

import com.ccojocea.aanews.models.entity.ArticleEntity;

import java.util.List;

/**
 * One page of results from the API, together with the total number of results available.
 */
public class ArticlePage {

    @NonNull
    public final List<ArticleEntity> articles;

    public final int totalResults;

    public ArticlePage(@NonNull List<ArticleEntity> articles, int totalResults) {
        this.articles = articles;
        this.totalResults = totalResults;
    }

}
