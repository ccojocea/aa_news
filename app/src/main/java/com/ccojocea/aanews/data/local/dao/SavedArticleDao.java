package com.ccojocea.aanews.data.local.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Query;

import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

@Dao
public abstract class SavedArticleDao implements BaseDao<SavedArticleEntity> {

    @Query("SELECT * FROM SavedArticleEntity ORDER BY publishedAt DESC")
    public abstract Observable<List<SavedArticleEntity>> loadSavedArticles();

    @Query("SELECT url FROM SavedArticleEntity")
    public abstract Observable<List<String>> loadSavedUrls();

    // '=' instead of LIKE: '_' and '%' are wildcards for LIKE and are common in urls
    @Query("DELETE FROM SavedArticleEntity WHERE url = :url")
    public abstract Completable deleteSavedArticle(@NonNull String url);

}
