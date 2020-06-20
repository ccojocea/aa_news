package com.ccojocea.aanews.data.local.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Query;

import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

@Dao
public abstract class SavedArticleDao implements BaseDao<SavedArticleEntity> {

    @Query("SELECT * FROM SavedArticleEntity")
    public abstract Observable<List<SavedArticleEntity>> loadSavedArticles();

    @Query("SELECT * FROM SavedArticleEntity")
    public abstract Single<List<SavedArticleEntity>> singleLoadOfArticles();

    @Query("DELETE FROM SavedArticleEntity WHERE url LIKE :url")
    public abstract Completable deleteSavedArticle(@NonNull String url);

}
