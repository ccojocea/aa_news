package com.ccojocea.aanews.ui.common;

import androidx.annotation.NonNull;

import com.ccojocea.aanews.data.remote.NewsWebService;
import com.ccojocea.aanews.models.ArticlePage;

import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.SerialDisposable;

/**
 * Articles loaded page by page from the API, see {@link #loadMore()}.
 */
public abstract class PagedArticlesViewModel extends ArticlesViewModel {

    private static final int FIRST_PAGE = 1;

    // only one request at a time, a new one cancels the previous (e.g. a new search while a page is loading)
    private final SerialDisposable pageRequest = new SerialDisposable();

    private int nextPage = FIRST_PAGE;
    private int totalResults;
    private boolean isLoading;
    private boolean isEndReached;

    public PagedArticlesViewModel() {
        compositeDisposable.add(pageRequest);
    }

    /**
     * @param page 1-based page index
     */
    @NonNull
    protected abstract Single<ArticlePage> loadPage(int page);

    /**
     * Loads the first page, replacing the current articles.
     */
    public void reload() {
        isLoading = true;
        isEndReached = false;
        setLoading(true);
        pageRequest.set(loadPage(FIRST_PAGE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(page -> {
                    isLoading = false;
                    setLoading(false);
                    onPageLoaded(FIRST_PAGE, page);
                    setArticles(page.articles);
                }, throwable -> {
                    isLoading = false;
                    setLoading(false);
                    onError(throwable, "Error while loading articles");
                })
        );
    }

    /**
     * Loads the next page if there is one and nothing is loading. Called when the list is scrolled near its end.
     */
    public void loadMore() {
        if (isLoading || !hasMore()) {
            return;
        }
        isLoading = true;
        final int page = nextPage;
        pageRequest.set(loadPage(page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    isLoading = false;
                    onPageLoaded(page, result);
                    appendArticles(result.articles);
                }, throwable -> {
                    isLoading = false;
                    // don't retry on every scroll, a refresh starts over
                    isEndReached = true;
                    onError(throwable, "Error while loading more articles");
                })
        );
    }

    /**
     * Cancels any request and shows an empty list.
     */
    protected void clear() {
        pageRequest.set(null);
        isLoading = false;
        isEndReached = true;
        nextPage = FIRST_PAGE;
        totalResults = 0;
        setLoading(false);
        setArticles(new ArrayList<>());
    }

    private void onPageLoaded(int page, @NonNull ArticlePage result) {
        nextPage = page + 1;
        totalResults = result.totalResults;
        if (result.articles.isEmpty()) {
            isEndReached = true;
        }
    }

    private boolean hasMore() {
        int requested = (nextPage - 1) * NewsWebService.PAGE_SIZE;
        return !isEndReached && requested < Math.min(totalResults, NewsWebService.MAX_RESULTS);
    }

}
