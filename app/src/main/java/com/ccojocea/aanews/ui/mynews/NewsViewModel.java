package com.ccojocea.aanews.ui.mynews;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class NewsViewModel extends ViewModel {

    protected final NewsRepository newsRepository;

    private final MutableLiveData<Boolean> errorLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<List<ArticleEntity>> articlesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public NewsViewModel() {
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
                    errorLiveData.setValue(true);
                })
        );
    }

    public MutableLiveData<List<ArticleEntity>> getArticlesLiveData() {
        return articlesLiveData;
    }

    public LiveData<Boolean> getErrorLiveData() {
        return errorLiveData;
    }

    public void resetError() {
        errorLiveData.setValue(false);
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
                    errorLiveData.setValue(true);
                })
        );
    }

    public void saveArticle(ArticleEntity articleEntity) {
        compositeDisposable.add(newsRepository.saveArticle(SavedArticleEntity.fromArticleEntity(articleEntity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Article saved");
                }, throwable -> {
                    Timber.e(throwable, "Error while saving article");
                    errorLiveData.setValue(true);
                })
        );
    }

    public void deleteArticle(String url) {
        compositeDisposable.add(newsRepository.deleteSavedArticle(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Article deleted");
                }, throwable -> {
                    Timber.e(throwable, "Error while deleting article");
                    errorLiveData.setValue(true);
                })
        );
    }

}
