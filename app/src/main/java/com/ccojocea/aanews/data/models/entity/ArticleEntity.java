package com.ccojocea.aanews.data.models.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class ArticleEntity {

    @PrimaryKey
    @NonNull
    public String url;

    public Source source;

    public String author;
    public String title;
    public String description;
    public String urlToImage;
    public String publishedAt;
    public String content;

    public ArticleEntity(@NonNull String url, Source source, String author, String title, String description, String urlToImage, String publishedAt, String content) {
        super();
        this.url = url;
        this.source = source;
        this.author = author;
        this.title = title;
        this.description = description;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        this.content = content;
    }

    static public class Source {

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        public Source(String id, String name) {
            super();
            this.id = id;
            this.name = name;
        }
    }
}
