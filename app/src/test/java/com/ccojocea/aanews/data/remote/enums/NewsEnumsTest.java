package com.ccojocea.aanews.data.remote.enums;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NewsEnumsTest {

    @Test
    public void country_valueIsLowercase() {
        assertEquals("ro", NewsCountry.RO.getValue());
    }

    @Test
    public void country_fromValue() {
        assertEquals(NewsCountry.GB, NewsCountry.fromValue("gb"));
        assertEquals(NewsCountry.GB, NewsCountry.fromValue("GB"));
        assertNull(NewsCountry.fromValue("xx"));
        assertNull(NewsCountry.fromValue(null));
    }

    @Test
    public void country_fromLocale_fallsBackToDefault() {
        assertEquals(NewsCountry.RO, NewsCountry.fromLocale(new Locale("ro", "RO")));
        assertEquals(NewsCountry.DEFAULT, NewsCountry.fromLocale(new Locale("es", "ES")));
        assertEquals(NewsCountry.DEFAULT, NewsCountry.fromLocale(new Locale("en")));
    }

    @Test
    public void country_searchNames_includeEnglishName() {
        assertTrue(NewsCountry.RO.getSearchNames().contains("Romania"));
        assertTrue(NewsCountry.US.getSearchNames().contains("United States"));
    }

    @Test
    public void language_fromValue() {
        assertEquals(NewsLanguage.SV, NewsLanguage.fromValue("sv"));
        assertEquals(NewsLanguage.ALL, NewsLanguage.fromValue(""));
        assertEquals(NewsLanguage.ALL, NewsLanguage.fromValue(null));
        assertNull(NewsLanguage.ALL.getValue());
    }

    @Test
    public void category_fromValue() {
        assertEquals(NewsCategory.SCIENCE, NewsCategory.fromValue("science"));
        assertEquals(NewsCategory.ALL, NewsCategory.fromValue(null));
        assertEquals(NewsCategory.ALL, NewsCategory.fromValue("unknown"));
    }

    @Test
    public void sortBy_fromValue() {
        assertEquals(NewsSortBy.POPULARITY, NewsSortBy.fromValue("popularity"));
        assertEquals(NewsSortBy.DEFAULT, NewsSortBy.fromValue(null));
    }

}
