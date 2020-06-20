package com.ccojocea.aanews.models.dto;

import android.media.tv.TvView;

import com.ccojocea.aanews.models.entity.ArticleEntity;

import timber.log.Timber;

public class ArticleDto {

    public String url;
    public Source source;
    public String author;
    public String title;
    public String description;
    public String urlToImage;
    public String publishedAt;
    public String content;

    public ArticleDto(
            String url,
            Source source,
            String author,
            String title,
            String description,
            String urlToImage,
            String publishedAt,
            String content) {
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
        public String id;
        public String name;

        public Source() {}

        public Source(String id, String name) {
            super();
            this.id = id;
            this.name = name;
        }
    }

    public ArticleEntity toArticleEntity(boolean isSaved) {
        if (urlToImage != null) {
            if (urlToImage.startsWith("//")) {
                try {
                    urlToImage = urlToImage.replace("//", url.substring(0, url.indexOf("/")));
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
            if (urlToImage.startsWith("/")) {
                try {
                    urlToImage = urlToImage.replace("/", url.substring(0, url.indexOf("/")));
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        }
        return new ArticleEntity(
                url,
                source != null ? new ArticleEntity.Source(source.id, source.name) : null,
                author,
                title,
                description,
                urlToImage,
                publishedAt,
                content,
                isSaved);
    }

}
