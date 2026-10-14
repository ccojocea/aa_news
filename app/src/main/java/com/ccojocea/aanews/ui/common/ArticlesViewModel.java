package com.ccojocea.aanews.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.common.Event;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.ui.settings.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * Base for the screens showing a list of articles.
 * Keeps the bookmark state of the articles in sync with the bookmarks saved in the database.
 */
public abstract class ArticlesViewModel extends ViewModel {

    protected final NewsRepository newsRepository;
    protected final PreferenceHelper preferenceHelper;
    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<List<ArticleEntity>> articlesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> errorLiveData = new MutableLiveData<>();

    // null until something was loaded, so the empty state isn't shown while the first request runs
    @Nullable
    private List<ArticleEntity> articles;
    @NonNull
    private Set<String> savedUrls = new HashSet<>();

    public ArticlesViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
        preferenceHelper = App.getAppComponent().preferenceHelper();

        compositeDisposable.add(newsRepository.listenToBookmarkedUrls()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(urls -> {
                    savedUrls = urls;
                    publishArticles();
                }, throwable -> Timber.e(throwable, "Error while listening to bookmarks"))
        );
    }

    public LiveData<List<ArticleEntity>> getArticlesLiveData() {
        return articlesLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<Event<String>> getErrorLiveData() {
        return errorLiveData;
    }

    protected void setArticles(@NonNull List<? extends ArticleEntity> newArticles) {
        // the API sometimes returns the same article twice, the url identifies it in the list
        Map<String, ArticleEntity> byUrl = new LinkedHashMap<>();
        for (ArticleEntity article : newArticles) {
            if (!byUrl.containsKey(article.getUrl())) {
                byUrl.put(article.getUrl(), article);
            }
        }
        articles = new ArrayList<>(byUrl.values());
        publishArticles();
    }

    protected void appendArticles(@NonNull List<ArticleEntity> moreArticles) {
        List<ArticleEntity> all = articles != null ? new ArrayList<>(articles) : new ArrayList<>();
        all.addAll(moreArticles);
        setArticles(all);
    }

    protected void setLoading(boolean isLoading) {
        loadingLiveData.setValue(isLoading);
    }

    protected void onError(@NonNull Throwable throwable, @NonNull String logMessage) {
        Timber.e(throwable, logMessage);
        errorLiveData.setValue(new Event<>(Utils.getErrorMessage(throwable)));
    }

    private void publishArticles() {
        if (articles == null) {
            return;
        }
        // always publish new instances, the adapter diffs the old list against the new one
        List<ArticleEntity> result = new ArrayList<>(articles.size());
        for (ArticleEntity article : articles) {
            result.add(article.withSaved(savedUrls.contains(article.getUrl())));
        }
        articlesLiveData.setValue(result);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

}
