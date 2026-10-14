package com.ccojocea.aanews.data;

import androidx.annotation.NonNull;

import com.ccojocea.aanews.data.local.AppDatabase;
import com.ccojocea.aanews.data.remote.NewsWebService;
import com.ccojocea.aanews.data.remote.enums.NewsCategory;
import com.ccojocea.aanews.data.remote.enums.NewsCountry;
import com.ccojocea.aanews.data.remote.enums.NewsLanguage;
import com.ccojocea.aanews.data.remote.enums.NewsSortBy;
import com.ccojocea.aanews.models.ArticlePage;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;
import com.ccojocea.aanews.models.responses.ArticlesResponse;

import java.util.ArrayList;
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

    // "My News" is cached and not paged, so it gets one bigger page
    private static final int MY_NEWS_PAGE_SIZE = 50;

    private final AppDatabase database;

    @Inject
    protected NewsWebService newsWebService;

    @Inject
    public NewsRepository() {
        database = AppDatabase.getInstance();
        Timber.d("Dagger debug - NewsRepository()");
    }

    /* Bookmarks */

    public Completable bookmarkArticle(@NonNull SavedArticleEntity entity) {
        return database.savedArticleDao().insert(entity)
                .subscribeOn(Schedulers.io());
    }

    public Completable removeBookmarkedArticle(@NonNull String url) {
        return database.savedArticleDao().deleteSavedArticle(url)
                .subscribeOn(Schedulers.io());
    }

    public Observable<List<SavedArticleEntity>> listenToBookmarks() {
        return database.savedArticleDao().loadSavedArticles()
                .subscribeOn(Schedulers.io());
    }

    /**
     * Emits the urls of all bookmarked articles each time the bookmarks change.
     * Lists from the API are matched against it to show which articles are saved.
     */
    public Observable<Set<String>> listenToBookmarkedUrls() {
        return database.savedArticleDao().loadSavedUrls()
                .map(urls -> (Set<String>) new HashSet<>(urls))
                .subscribeOn(Schedulers.io());
    }

    /* Headlines and search */

    /**
     * @param page 1-based page index
     */
    public Single<ArticlePage> getTopHeadlines(@NonNull NewsCountry country, @NonNull NewsCategory category, int page) {
        return newsWebService.fetchTopHeadlines(country.getValue(), category.getValue(), page, NewsWebService.PAGE_SIZE)
                .map(NewsRepository::toArticlePage)
                .subscribeOn(Schedulers.io());
    }

    /**
     * @param page 1-based page index
     */
    public Single<ArticlePage> searchArticles(@NonNull String query, @NonNull NewsLanguage language, @NonNull NewsSortBy sortBy, int page) {
        return newsWebService.fetchEverything(query, language.getValue(), sortBy.getValue(), page, NewsWebService.PAGE_SIZE)
                .map(NewsRepository::toArticlePage)
                .subscribeOn(Schedulers.io());
    }

    /* My News */

    /**
     * Emits the cached "My News" articles, newest first, each time the cache changes.
     */
    public Observable<List<ArticleEntity>> listenToMyNews() {
        return database.articleDao().loadArticles()
                .subscribeOn(Schedulers.io());
    }

    /**
     * Fetches the newest articles about any of the topics and replaces the cache with them.
     */
    public Completable refreshMyNews(@NonNull List<String> topics, @NonNull NewsLanguage language) {
        if (topics.isEmpty()) {
            return Completable.fromAction(() -> database.articleDao().deleteArticles())
                    .subscribeOn(Schedulers.io());
        }
        return newsWebService.fetchEverything(Topics.buildQuery(topics), language.getValue(), NewsSortBy.PUBLISHED_AT.getValue(), 1, MY_NEWS_PAGE_SIZE)
                .map(NewsRepository::toArticles)
                .flatMapCompletable(articles -> Completable.fromAction(() -> database.articleDao().replaceArticles(articles)))
                .subscribeOn(Schedulers.io());
    }

    /* Mapping */

    @NonNull
    private static List<ArticleEntity> toArticles(@NonNull ArticlesResponse response) {
        if (response.articles == null) {
            return new ArrayList<>();
        }
        // the saved state is not known here, it is matched against the bookmarks by the view models
        return response.articles.stream()
                .filter(articleDto -> articleDto != null && articleDto.url != null)
                .map(articleDto -> articleDto.toArticleEntity(false))
                .collect(Collectors.toList());
    }

    @NonNull
    private static ArticlePage toArticlePage(@NonNull ArticlesResponse response) {
        return new ArticlePage(toArticles(response), response.totalResults);
    }

}
