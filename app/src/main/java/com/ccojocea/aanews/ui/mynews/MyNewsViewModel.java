package com.ccojocea.aanews.ui.mynews;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.NewsHelper;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;
import com.ccojocea.aanews.ui.settings.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.internal.Util;
import timber.log.Timber;

public class MyNewsViewModel extends ViewModel {

    protected final NewsRepository newsRepository;

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<List<ArticleEntity>> articlesLiveData = new MutableLiveData<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MyNewsViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
        listenToDatabaseArticles();
        refreshData();
    }

    private void listenToDatabaseArticles() {
        compositeDisposable.add(newsRepository.listenToAllAndroidArticles()
                // When the results come back, make sure we switch to main thread to handle them
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(articles -> {
                    Timber.d("Received response: %s", articles);
                    articlesLiveData.setValue(articles);
                }, throwable -> {
                    // Handle the error
                    Timber.e(throwable, "Received error while fetching articles:");
                    errorLiveData.setValue(Utils.getErrorMessage(throwable));
                })
        );
    }

    public MutableLiveData<List<ArticleEntity>> getArticlesLiveData() {
        return articlesLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void resetError() {
        errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public void refreshData() {
        compositeDisposable.add(newsRepository.fetchArticles()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Data fetch completed");
                }, throwable -> {
                    Timber.e(throwable, "Received error while fetching articles:");
                    errorLiveData.setValue(Utils.getErrorMessage(throwable));
                })
        );
    }

}
