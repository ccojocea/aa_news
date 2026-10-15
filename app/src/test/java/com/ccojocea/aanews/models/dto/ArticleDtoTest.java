package com.ccojocea.aanews.models.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ArticleDtoTest {

    private static final String ARTICLE_URL = "https://www.example.com/news/2020/article.html";

    @Test
    public void resolveImageUrl_keepsAbsoluteUrls() {
        assertEquals("https://cdn.example.com/a.jpg", ArticleDto.resolveImageUrl(ARTICLE_URL, "https://cdn.example.com/a.jpg"));
        assertEquals("http://cdn.example.com/a.jpg", ArticleDto.resolveImageUrl(ARTICLE_URL, " http://cdn.example.com/a.jpg "));
    }

    @Test
    public void resolveImageUrl_protocolRelative() {
        assertEquals("https://cdn.example.com/img/a.jpg", ArticleDto.resolveImageUrl(ARTICLE_URL, "//cdn.example.com/img/a.jpg"));
    }

    @Test
    public void resolveImageUrl_relativeToTheArticle() {
        assertEquals("https://www.example.com/img/a.jpg", ArticleDto.resolveImageUrl(ARTICLE_URL, "/img/a.jpg"));
    }

    @Test
    public void resolveImageUrl_missingOrInvalid() {
        assertNull(ArticleDto.resolveImageUrl(ARTICLE_URL, null));
        assertNull(ArticleDto.resolveImageUrl(ARTICLE_URL, " "));
        assertNull(ArticleDto.resolveImageUrl(null, "/img/a.jpg"));
        assertNull(ArticleDto.resolveImageUrl(ARTICLE_URL, "/img/a b|c.jpg"));
    }

    @Test
    public void cleanTitle_removesPublisherSuffix() {
        assertEquals("Some headline", ArticleDto.cleanTitle("Some headline - Digi24", "Digi24", "Google News"));
        assertEquals("Some headline", ArticleDto.cleanTitle("Some headline - The Verge", null, "The Verge"));
    }

    @Test
    public void cleanTitle_keepsOtherDashes() {
        assertEquals("Covid-19 - what we know", ArticleDto.cleanTitle("Covid-19 - what we know", "Reuters", "Reuters"));
        // a title that is only the publisher is left as it is, not emptied
        assertEquals("- Digi24", ArticleDto.cleanTitle(" - Digi24", "Digi24", null));
        assertNull(ArticleDto.cleanTitle(null, "Digi24", null));
    }

    @Test
    public void isRemoved() {
        assertTrue(dto("https://removed.com", "[Removed]").isRemoved());
        assertTrue(dto(null, "Title").isRemoved());
        assertFalse(dto(ARTICLE_URL, "Title").isRemoved());
    }

    private static ArticleDto dto(String url, String title) {
        return new ArticleDto(url, null, null, title, null, null, "2020-06-12T14:22:06Z", null);
    }

}
