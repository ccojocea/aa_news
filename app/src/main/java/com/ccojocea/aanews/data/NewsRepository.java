package com.ccojocea.aanews.data;

import com.ccojocea.aanews.data.local.AppDatabase;
import com.ccojocea.aanews.data.local.dao.SavedArticleDao;
import com.ccojocea.aanews.models.dto.ArticleDto;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.data.remote.NewsWebService;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class NewsRepository {

    private static NewsRepository instance;
    private NewsWebService newsWebService;

    private NewsRepository() {
        newsWebService = new NewsWebService();
    }

    public static NewsRepository getInstance() {
        if (instance == null) {
            return new NewsRepository();
        }
        return instance;
    }

    public Completable deleteSavedArticle(String url) {
        return AppDatabase.getInstance().articleDao().updateArticle(url, false)
                .andThen(AppDatabase.getInstance().savedArticleDao().deleteSavedArticle(url))
                .subscribeOn(Schedulers.io());
    }

    public Completable saveArticle(SavedArticleEntity entity) {
        return AppDatabase.getInstance().articleDao().updateArticle(entity.getUrl(), true)
                .andThen(AppDatabase.getInstance().savedArticleDao().insert(entity))
                .subscribeOn(Schedulers.io());
    }

    //TODO Delete if not needed
//    public Single<List<SavedArticleEntity>> getBookmarks() {
//        return AppDatabase.getInstance().savedArticleDao().loadSavedArticles()
//                .map(savedArticleEntities -> savedArticleEntities.stream()
//                        .map(savedArticleEntity -> (ArticleEntity) savedArticleEntity)
//                        .collect(Collectors.toList()))
//                .subscribeOn(Schedulers.io());
//    }

    public Observable<List<ArticleEntity>> listenToBookmarks() {
        return AppDatabase.getInstance().savedArticleDao().loadSavedArticles()
                .map(savedArticleEntities -> savedArticleEntities.stream()
                        .map(savedArticleEntity -> (ArticleEntity) savedArticleEntity)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    public Single<List<ArticleEntity>> getTopHeadlines() {
        Set<String> savedUrlSet = new HashSet<>();
        Single<Set<String>> singleSet;
//        Single<Set<String>> fff = AppDatabase.getInstance().savedArticleDao().singleLoadOfArticles()
//                .map(savedArticleEntities -> savedArticleEntities.stream()
//                        .map(savedArticleEntity -> {
//                            savedUrlSet.add(savedArticleEntity.getUrl());
//                            return savedArticleEntity.getUrl();
//                        })
//                        .collect(Collectors.toSet()))
//                .subscribeOn(Schedulers.io());


        //first get bookmarked items - this works
//        Completable first = Completable.fromSingle(singleSet = AppDatabase.getInstance().savedArticleDao().singleLoadOfArticles()
//                .map(savedArticleEntities -> {
//                    return savedArticleEntities.stream().map(savedArticleEntity -> savedArticleEntity.getUrl()).collect(Collectors.toSet());
//                })
//                .subscribeOn(Schedulers.io()));
//
//        Single<List<ArticleEntity>> second =
//                newsWebService.fetchTopHeadlines()
//                        .subscribeOn(Schedulers.io())
//                        .map(list -> list.stream()
//                            .map(articleDto -> articleDto.toArticleEntity(singleSet.)
//                            .collect(Collectors.toList()));
//
//        return first.andThen(second);

//        //first get bookmarked items - this works
        Completable first = Completable.fromSingle(AppDatabase.getInstance().savedArticleDao().singleLoadOfArticles()
                .map(savedArticleEntities -> {
                    List<String> strings = new ArrayList<>();
                    savedArticleEntities.stream().forEach(savedArticleEntity ->
                    {
                        strings.add(savedArticleEntity.getUrl());
                        savedUrlSet.add(savedArticleEntity.getUrl());
                    });
                    return strings;
                })
                .subscribeOn(Schedulers.io()));

        //get top headlines from API and then check which ones were bookmarked
        Single<List<ArticleEntity>> second =
                newsWebService.fetchTopHeadlines()
                        .subscribeOn(Schedulers.io())
                        .map(list -> list.stream()
                            .filter(articleDto -> articleDto.url != null)
                            .map(articleDto -> articleDto.toArticleEntity(savedUrlSet.contains(articleDto.url)))
                            .collect(Collectors.toList()));

        return first.andThen(second);
    }

    //This works but needs to check database
//    public Single<List<ArticleEntity>> getTopHeadlines() {
//        return newsWebService.fetchTopHeadlines()
//                .map(list -> list.stream()
//                        .map(articleDto -> articleDto.toArticleEntity())
//                        .collect(Collectors.toList()))
//                .subscribeOn(Schedulers.io());
//    }

    public Single<List<ArticleEntity>> getPagedTopHeadlines(int page) {
        return newsWebService.fetchPagedTopHeadlines(page)
                .map(list -> list.stream()
                        .map(articleDto -> articleDto.toArticleEntity(false))
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    //TODO From AA
    public Observable<List<ArticleEntity>> listenToAllAndroidArticles() {
        return AppDatabase.getInstance().articleDao().loadArticles()
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
                    return AppDatabase.getInstance().articleDao().insertAll(articleEntities);
                })
                // Make sure that the request above runs on a background thread, dedicated to I/O
                .subscribeOn(Schedulers.io());
    }


    /////////////////////////////////////////////////////////////
    //TODO REMOVE

    public static List<Integer> test() {
        Function<String, Integer> toInt = Integer::parseInt;
        List<String> strings = new ArrayList<>();
        strings.add("3");
        strings.add("");
        strings.add("a");
        strings.add("a");
        strings.add(null);
        List<Integer> ints = strings.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .map(s -> stringToInt(s))
                .collect(Collectors.toList());
        return ints;
    }

    public static List<Integer> test2() {
        List<List<String>> wrapper = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        List<String> strings2 = new ArrayList<>();
        strings.add("3");
        strings.add("4");
        strings2.add("5");
        wrapper.add(strings);
        wrapper.add(strings2);
        List<Integer> intWrapper = wrapper.stream()
                .flatMap(stringsList -> stringsList.stream()
                        .map(s -> Integer.parseInt(s)))
                .collect(Collectors.toList());
        return intWrapper;
    }

    public static List<List<Integer>> test3() {
        List<List<String>> wrapper = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        List<String> strings2 = new ArrayList<>();
        strings.add("3");
        strings.add("4");
        strings2.add("5");
        wrapper.add(strings);
        wrapper.add(strings2);
        List<List<Integer>> intWrapper = wrapper.stream()
                .map(stringsList -> {
                    List<Integer> ints = stringsList.stream()
                            .map(s -> Integer.parseInt(s))
                            .collect(Collectors.toList());
                    return ints;
                })
                .collect(Collectors.toList());
        return intWrapper;
    }

    private static Integer stringToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
