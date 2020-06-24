package com.ccojocea.aanews.models.entity;

import android.os.Build;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class ArticleEntity implements Comparable<ArticleEntity>, Cloneable {

    @PrimaryKey
    @NonNull
    protected final String url;

    @NonNull
    protected final Source source;

    @NonNull
    protected final String author;

    @NonNull
    protected final String title;

    @Nullable
    protected final String urlToImage;

    @NonNull
    protected final String publishedAt;

    @Nullable
    protected final String description;

    @NonNull
    protected final String content;

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
            @Nullable String content,
            boolean isSaved) {
        super();
        this.url = url;
        this.source = source != null ? source : new Source(null, "");
        this.author = author != null ? author : "";
        this.title = title != null ? title : "";
        this.description = description != null ? !description.isEmpty() ? description : null : null;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt != null ? publishedAt : "";
        if (content != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                //noinspection deprecation
                this.content = Html.fromHtml(content).toString();
            }
        } else {
            this.content = "";
        }
        this.isSaved = isSaved;
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

    @NonNull
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

    @NonNull
    @Override
    protected Object clone() {
        ArticleEntity clone;
        try {
            clone = (ArticleEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return clone;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;

        if (obj instanceof ArticleEntity) {
            ArticleEntity articleEntity = (ArticleEntity) obj;
            return articleEntity.getUrl().equals(this.getUrl());
        }
        return false;
    }

    @Override
    public int compareTo(ArticleEntity compare) {
        if (compare.url.equals(this.url) && compare.isSaved == this.isSaved) {
            return 0;
        }
        return 1;
    }

    static public class Source implements Cloneable {

        @SerializedName("id")
        @Nullable
        protected final String id;

        @SerializedName("name")
        @NonNull
        protected final String name;

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

        @NonNull
        @Override
        protected Object clone() {
            Source clone;
            try {
                clone = (Source) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }

            return clone;
        }

    }

}
