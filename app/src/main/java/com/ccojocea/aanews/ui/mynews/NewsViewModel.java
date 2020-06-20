package com.ccojocea.aanews.ui.mynews;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class NewsViewModel extends ViewModel {

    private MutableLiveData<Boolean> errorLiveData = new MutableLiveData<>(false);
    private MutableLiveData<List<ArticleEntity>> articlesLiveData = new MutableLiveData<>(new ArrayList<>());
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public NewsViewModel() {
        listenToDatabaseArticles();
        refreshData();
    }

    private void listenToDatabaseArticles() {
        compositeDisposable.add(NewsRepository.getInstance().listenToAllAndroidArticles()
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
        compositeDisposable.add(NewsRepository.getInstance().fetchArticles()
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
        compositeDisposable.add(NewsRepository.getInstance().saveArticle(SavedArticleEntity.fromArticleEntity(articleEntity))
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
        compositeDisposable.add(NewsRepository.getInstance().deleteSavedArticle(url)
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
