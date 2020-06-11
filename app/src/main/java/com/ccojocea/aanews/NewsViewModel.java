package com.ccojocea.aanews;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.data.models.entity.ArticleEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class NewsViewModel extends ViewModel {

    private MutableLiveData<Boolean> errorLiveData = new MutableLiveData<>(false);
    private MutableLiveData<List<ArticleEntity>> topHeadlinesLiveData = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<ArticleEntity>> articlesLiveData = new MutableLiveData<>(new ArrayList<>());
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public NewsViewModel() {
        //TODO
        getTopHeadlines();

//        listenToDatabaseArticles();

//        new Handler().postDelayed(() -> {
//            compositeDisposable.add(NewsRepository.getInstance().fetchArticles()
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(() -> {
//                        Timber.d("Data fetch completed");
//                    }, throwable -> {
//                        Timber.e(throwable, "Data fetch failed");
//                    })
//            );
//        }, 1000L);
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

    public LiveData<List<ArticleEntity>> getTopHeadlinesLiveData() {
        return topHeadlinesLiveData;
    }

    public LiveData<Boolean> getErrorLiveData() {
        return errorLiveData;
    }

    private void getTopHeadlines() {
        compositeDisposable.add(
                NewsRepository.getInstance()
                        .getTopHeadlines()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(articleEntities -> {
                            topHeadlinesLiveData.setValue(articleEntities);
                        }, throwable -> {
                            Timber.e(throwable, "Error while fetching top headlines");
                            errorLiveData.setValue(true);
                        })
        );
    }

    public void resetError() {
        errorLiveData.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
