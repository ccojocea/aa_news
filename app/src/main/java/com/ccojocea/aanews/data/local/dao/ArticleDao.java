package com.ccojocea.aanews.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.ccojocea.aanews.models.entity.ArticleEntity;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

@Dao
public abstract class ArticleDao implements BaseDao<ArticleEntity>{

    @Query("SELECT * FROM ArticleEntity WHERE url LIKE :urlKey LIMIT 1")
    public abstract Observable<ArticleEntity> loadArticle(String urlKey);

    @Query("SELECT * FROM ArticleEntity")
    public abstract Observable<List<ArticleEntity>> loadArticles();

    // all db operations in this method will be run inside one transaction
    // this fails if any exception is thrown inside the body
    @Transaction
    public void deleteAndInsertOther(List<ArticleEntity> articleEntities) {
        deleteArticles();
        insertAll(articleEntities);
    }

    @Query("DELETE FROM ArticleEntity")
    public abstract int deleteArticles();

    @Query("UPDATE ArticleEntity SET isSaved = :isSaved WHERE url LIKE :url")
    public abstract Completable updateArticle(String url, boolean isSaved);

}
