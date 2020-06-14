package com.ccojocea.aanews.data;

import com.ccojocea.aanews.data.local.AppDatabase;
import com.ccojocea.aanews.data.models.dto.ArticleDto;
import com.ccojocea.aanews.data.models.entity.ArticleEntity;
import com.ccojocea.aanews.data.remote.NewsWebService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
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

    public Single<List<ArticleEntity>> getTopHeadlines() {
        return newsWebService.fetchTopHeadlines()
                .map(list -> list.stream()
                        .map(articleDto -> articleDto.toArticleEntity())
                .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    public Single<List<ArticleEntity>> getPagedTopHeadlines(int page) {
        return newsWebService.fetchPagedTopHeadlines(page)
                .map(list -> list.stream()
                        .map(articleDto -> articleDto.toArticleEntity())
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
                            .map(articleDto -> articleDto.toArticleEntity())
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

    private static Integer stringToInt (String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
