package com.ccojocea.aanews.data;

import com.ccojocea.aanews.data.local.AppDatabase;
import com.ccojocea.aanews.data.remote.NewsWebService;
import com.ccojocea.aanews.models.dto.ArticleDto;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Singleton
public class NewsRepository {

    private final AppDatabase database;

    @Inject
    protected NewsWebService newsWebService;

    @Inject
    public NewsRepository() {
        database = AppDatabase.getInstance();
        Timber.d("Dagger debug - NewsRepository()");
    }

    public Completable removeBookmarkedArticle(String url) {
        return database.articleDao().updateArticle(url, false)
                .andThen(database.savedArticleDao().deleteSavedArticle(url))
                .subscribeOn(Schedulers.io());
    }

    public Completable bookmarkArticle(SavedArticleEntity entity) {
        return database.articleDao().updateArticle(entity.getUrl(), true)
                .andThen(database.savedArticleDao().insert(entity))
                .subscribeOn(Schedulers.io());
    }

    public Observable<List<ArticleEntity>> listenToBookmarks() {
        return database.savedArticleDao().loadSavedArticles()
                .map(savedArticleEntities -> savedArticleEntities.stream()
                        .sorted(new Comparator<ArticleEntity>() {
                            @Override
                            public int compare(ArticleEntity o1, ArticleEntity o2) {
                                return o2.getPublishedAt().compareTo(o1.getPublishedAt());
                            }
                        })
                        .map(savedArticleEntity -> (ArticleEntity) savedArticleEntity)
                        .collect(Collectors.toList()))

                .subscribeOn(Schedulers.io());
    }

    public Single<List<ArticleEntity>> getTopHeadlines() {
        Set<String> savedUrlSet = new HashSet<>();

        //first get bookmarked items
        Completable first = Completable.fromSingle(database.savedArticleDao().singleLoadOfArticles()
                .map(savedArticleEntities -> {
                    List<String> strings = new ArrayList<>();
                    savedArticleEntities.forEach(savedArticleEntity ->
                    {
                        strings.add(savedArticleEntity.getUrl());
                        savedUrlSet.add(savedArticleEntity.getUrl());
                    });
                    return strings;
                })
                .subscribeOn(Schedulers.io()));

        //get top headlines from API and then check which ones were bookmarked
        Single<List<ArticleEntity>> second = newsWebService.fetchTopHeadlines()
                .subscribeOn(Schedulers.io())
                .map(list -> list.stream()
                        .filter(articleDto -> articleDto.url != null)
                        .map(articleDto -> articleDto.toArticleEntity(savedUrlSet.contains(articleDto.url)))
                        .collect(Collectors.toList()));

        return first.andThen(second);
    }

    public Single<List<ArticleEntity>> searchArticles(String query, String language) {
        Set<String> savedUrlSet = new HashSet<>();

        //first get bookmarked items
        Completable first = Completable.fromSingle(database.savedArticleDao().singleLoadOfArticles()
                .map(savedArticleEntities -> {
                    List<String> strings = new ArrayList<>();
                    savedArticleEntities.forEach(savedArticleEntity ->
                    {
                        strings.add(savedArticleEntity.getUrl());
                        savedUrlSet.add(savedArticleEntity.getUrl());
                    });
                    return strings;
                })
                .subscribeOn(Schedulers.io()));

        Single<List<ArticleEntity>> second = newsWebService.fetchEverything(query, language)
                .subscribeOn(Schedulers.io())
                .map(articleDtos -> articleDtos.stream()
                        .filter(articleDto -> articleDto.url != null)
                        .map(articleDto -> articleDto.toArticleEntity(savedUrlSet.contains(articleDto.url)))
                        .collect(Collectors.toList()));

        return first.andThen(second);
    }

    //TODO Pagination
    public Single<List<ArticleEntity>> getPagedTopHeadlines(int page) {
        return newsWebService.fetchPagedTopHeadlines(page)
                .map(list -> list.stream()
                        .map(articleDto -> articleDto.toArticleEntity(false))
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    //TODO From AA
    public Observable<List<ArticleEntity>> listenToAllAndroidArticles() {
        return database.articleDao().loadArticles()
                // Make sure that the request above runs on a background thread, dedicated to I/O
                .subscribeOn(Schedulers.io());
    }

    //TODO From AA
    public Completable fetchArticles() {
        // First fetch the articles from the webservice
        return newsWebService.fetchEverything("Android")
                // ... then ...
                .flatMapCompletable((List<ArticleDto> articleDtos) -> {
                    // Convert models from DTO to Entity (can also be done using a for)
                    List<ArticleEntity> articleEntities = articleDtos.stream()
                            .filter(articleDto -> articleDto.url != null)
                            .map(articleDto -> articleDto.toArticleEntity(false))
                            .collect(Collectors.toList());
                    // And save them into the database
                    return database.articleDao().insertAll(articleEntities);
                })
                // Make sure that the request above runs on a background thread, dedicated to I/O
                .subscribeOn(Schedulers.io());
    }

}
