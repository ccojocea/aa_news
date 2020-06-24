package com.ccojocea.aanews.models.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;

@Entity
public class SavedArticleEntity extends ArticleEntity {

    public SavedArticleEntity(
            @NonNull String url,
            @Nullable Source source,
            @Nullable String author,
            @Nullable String title,
            @Nullable String description,
            @Nullable String urlToImage,
            @NonNull String publishedAt,
            @Nullable String content) {
        super(url, source, author, title, description, urlToImage, publishedAt, content, true);
    }

    public static SavedArticleEntity fromArticleEntity(ArticleEntity articleEntity) {
        return new SavedArticleEntity(
                articleEntity.url,
                articleEntity.source,
                articleEntity.author,
                articleEntity.title,
                articleEntity.description,
                articleEntity.urlToImage,
                articleEntity.publishedAt,
                articleEntity.content);
    }

}
