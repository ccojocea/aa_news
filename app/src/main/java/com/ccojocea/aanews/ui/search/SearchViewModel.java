package com.ccojocea.aanews.ui.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccojocea.aanews.data.remote.enums.NewsLanguage;
import com.ccojocea.aanews.data.remote.enums.NewsSortBy;
import com.ccojocea.aanews.models.ArticlePage;
import com.ccojocea.aanews.ui.common.PagedArticlesViewModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class SearchViewModel extends PagedArticlesViewModel {

    private static final int DELAY = 500;

    // the text typed by the user, searched once it stops changing for DELAY ms
    private final PublishSubject<String> queries = PublishSubject.create();

    @NonNull
    private String query = "";
    @NonNull
    private NewsSortBy sortBy;
    @NonNull
    private NewsLanguage language;

    public SearchViewModel() {
        sortBy = preferenceHelper.getSearchSortBy();
        language = preferenceHelper.getNewsLanguage();

        // subscribed once for the lifetime of the view model, the fragment only pushes text into it
        compositeDisposable.add(queries
                .map(String::trim)
                .debounce(DELAY, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::search, throwable -> Timber.e(throwable, "Error in the search query stream"))
        );

        // the current language is already used above, only react to changes
        compositeDisposable.add(preferenceHelper.listenToNewsLanguage()
                .skip(1)
                .subscribe(newLanguage -> {
                    language = newLanguage;
                    if (hasQuery()) {
                        reload();
                    }
                }, throwable -> Timber.e(throwable, "Error while listening to the language preference"))
        );
    }

    public void onQueryChanged(@Nullable String text) {
        queries.onNext(text != null ? text : "");
    }

    private void search(@NonNull String newQuery) {
        query = newQuery;
        if (newQuery.isEmpty()) {
            clear();
        } else {
            // cancels the request for the previous query, if it is still running
            reload();
        }
    }

    public boolean hasQuery() {
        return !query.isEmpty();
    }

    @NonNull
    public String getQuery() {
        return query;
    }

    @NonNull
    public NewsSortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(@NonNull NewsSortBy newSortBy) {
        if (newSortBy == sortBy) {
            return;
        }
        sortBy = newSortBy;
        preferenceHelper.setSearchSortBy(newSortBy);
        if (hasQuery()) {
            reload();
        }
    }

    @NonNull
    @Override
    protected Single<ArticlePage> loadPage(int page) {
        return newsRepository.searchArticles(query, language, sortBy, page);
    }

}
