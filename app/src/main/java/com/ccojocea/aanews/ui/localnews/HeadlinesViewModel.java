package com.ccojocea.aanews.ui.localnews;

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

public class HeadlinesViewModel extends ViewModel {

    protected final NewsRepository newsRepository;

    private static final int PAGE_SIZE = 20;

    private final MutableLiveData<Boolean> errorLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<List<ArticleEntity>> topHeadlinesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

//    public final LiveData<PagedList<ArticleEntity>> headlinesPagedLiveData;

    public HeadlinesViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
        getTopHeadlines();
//        getPagedTopHeadlines();
//        headlinesPagedLiveData = new LivePagedListBuilder<>();
    }

    public LiveData<List<ArticleEntity>> getTopHeadlinesLiveData() {
        return topHeadlinesLiveData;
    }

    public LiveData<Boolean> getErrorLiveData() {
        return errorLiveData;
    }

    public void getTopHeadlines() {
        compositeDisposable.add(
                newsRepository.getTopHeadlines()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(articleEntities -> {
                            topHeadlinesLiveData.setValue(articleEntities);
                        }, throwable -> {
                            Timber.e(throwable, "Error while fetching top headlines");
                            errorLiveData.setValue(true);
                        })
        );
    }

    private void getPagedTopHeadlines() {
        compositeDisposable.add(
                newsRepository
                        .getPagedTopHeadlines(PAGE_SIZE)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(articleEntities -> {
                            List<ArticleEntity> currentList = topHeadlinesLiveData.getValue();
                            if (currentList != null) currentList.addAll(articleEntities);
                            topHeadlinesLiveData.setValue(currentList);
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
