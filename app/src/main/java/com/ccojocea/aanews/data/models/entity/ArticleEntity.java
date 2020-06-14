package com.ccojocea.aanews.data.models.entity;

import android.os.Build;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class ArticleEntity {

    @PrimaryKey
    @NonNull
    protected String url;

    @NonNull
    protected Source source;

    @NonNull
    protected String author;

    @NonNull
    protected String title;

    @Nullable
    protected String urlToImage;

    @Nullable
    protected String publishedAt;

    @Nullable
    protected String description;

    @NonNull
    protected String content;

    //TODO
    //used locally for bookmarked articles
    protected boolean isSaved;

    public ArticleEntity(
            @NonNull String url,
            @Nullable Source source,
            @Nullable String author,
            @Nullable String title,
            @Nullable String description,
            @Nullable String urlToImage,
            @Nullable String publishedAt,
            @Nullable String content) {
        super();
        this.url = url;
        this.source = source != null ? source : new Source(null, "");
        this.author = author != null ? author : "";
        this.title = title != null ? title : "";
        this.description = description != null ? !description.isEmpty() ? description : null : null;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        if (content != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                this.content = Html.fromHtml(content).toString();
            }
        } else {
            this.content = "";
        }
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public Source getSource() {
        return source;
    }

    @NonNull
    public String getAuthor() {
        return author;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getUrlToImage() {
        return urlToImage;
    }

    @Nullable
    public String getPublishedAt() {
        return publishedAt;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean isSaved) {
        this.isSaved = isSaved;
    }

    static public class Source {

        @SerializedName("id")
        @Nullable
        protected String id;

        @SerializedName("name")
        @NonNull
        protected String name;

        public Source(@Nullable String id, @Nullable String name) {
            super();
            this.id = id;
            if (name != null) {
                this.name = name;
            } else {
                this.name = "";
            }
        }

        @NonNull
        public String getName() {
            return name;
        }

    }

}
