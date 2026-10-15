package com.ccojocea.aanews.models.dto;

import androidx.annotation.Nullable;

import com.ccojocea.aanews.models.entity.ArticleEntity;

import java.net.URI;

import timber.log.Timber;

public class ArticleDto {

    private static final String REMOVED_TITLE = "[Removed]";
    private static final String REMOVED_URL = "https://removed.com";
    private static final String GOOGLE_NEWS_ID = "google-news";

    public final String url;
    public final Source source;
    public final String author;
    public final String title;
    public final String description;
    public final String urlToImage;
    public final String publishedAt;
    public final String content;

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

    /**
     * NewsAPI keeps placeholders for articles that were taken down by their publisher.
     */
    public boolean isRemoved() {
        return url == null || REMOVED_TITLE.equals(title) || REMOVED_URL.equals(url);
    }

    public ArticleEntity toArticleEntity(boolean isSaved) {
        String sourceName = source != null ? source.name : null;
        // headlines of some countries come through Google News, the publisher is in the author field
        if (source != null && GOOGLE_NEWS_ID.equals(source.id) && author != null && !author.trim().isEmpty()) {
            sourceName = author;
        }
        return new ArticleEntity(
                url,
                source != null ? new ArticleEntity.Source(source.id, sourceName) : null,
                author,
                cleanTitle(title, author, sourceName),
                description,
                resolveImageUrl(url, urlToImage),
                publishedAt,
                content,
                isSaved);
    }

    /**
     * Some titles end with the publisher, e.g. "Some headline - Publisher", which is already shown on the card.
     */
    @Nullable
    static String cleanTitle(@Nullable String title, @Nullable String author, @Nullable String sourceName) {
        if (title == null) {
            return null;
        }
        String cleaned = title.trim();
        for (String publisher : new String[]{author, sourceName}) {
            if (publisher == null || publisher.trim().isEmpty()) {
                continue;
            }
            String suffix = " - " + publisher.trim();
            if (cleaned.endsWith(suffix) && cleaned.length() > suffix.length()) {
                cleaned = cleaned.substring(0, cleaned.length() - suffix.length()).trim();
            }
        }
        return cleaned;
    }

    /**
     * Makes protocol-relative ("//cdn.example.com/a.jpg") and relative ("/a.jpg") image urls absolute,
     * using the article url as the base.
     */
    @Nullable
    static String resolveImageUrl(@Nullable String articleUrl, @Nullable String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        String trimmed = imageUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (articleUrl == null) {
            return null;
        }
        try {
            return URI.create(articleUrl).resolve(trimmed).toString();
        } catch (IllegalArgumentException e) {
            Timber.w(e, "Could not resolve image url %s", imageUrl);
            return null;
        }
    }

}
