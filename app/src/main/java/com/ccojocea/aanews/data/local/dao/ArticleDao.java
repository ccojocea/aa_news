package com.ccojocea.aanews.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.ccojocea.aanews.models.entity.ArticleEntity;

import java.util.List;

import io.reactivex.Observable;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Cache for the "My News" articles.
 */
@Dao
public abstract class ArticleDao implements BaseDao<ArticleEntity> {

    // publishedAt is an ISO-8601 UTC timestamp, so sorting the text sorts by date
    @Query("SELECT * FROM ArticleEntity ORDER BY publishedAt DESC")
    public abstract Observable<List<ArticleEntity>> loadArticles();

    // all db operations in this method will be run inside one transaction
    // this fails if any exception is thrown inside the body
    @Transaction
    public void replaceArticles(List<ArticleEntity> articleEntities) {
        deleteArticles();
        insertArticles(articleEntities);
    }

    // blocking version used inside the transaction above, a Completable would never be subscribed there
    @Insert(onConflict = REPLACE)
    public abstract void insertArticles(List<ArticleEntity> articleEntities);

    @Query("DELETE FROM ArticleEntity")
    public abstract int deleteArticles();

}
