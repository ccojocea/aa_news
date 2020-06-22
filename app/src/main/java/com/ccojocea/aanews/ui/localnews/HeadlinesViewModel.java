package com.ccojocea.aanews.ui.localnews;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.internal.Util;
import timber.log.Timber;

public class HeadlinesViewModel extends ViewModel {

    protected final NewsRepository newsRepository;

    private static final int PAGE_SIZE = 20;

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<List<ArticleEntity>> topHeadlinesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

//    public final LiveData<PagedList<ArticleEntity>> headlinesPagedLiveData;

    public HeadlinesViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
        getTopHeadlines();
//        getPagedTopHeadlines();
//        headlinesPagedLiveData = new LivePagedListBuilder<>();

        // update the list if there's a change in the bookmark database
        compositeDisposable.add(newsRepository.listenToBookmarks()
                //keep the work in background due to iterations, update with postValue
                .observeOn(Schedulers.io())
                .subscribe(savedArticles -> {
                    List<ArticleEntity> headlineArticles = topHeadlinesLiveData.getValue();
                    if (headlineArticles != null && headlineArticles.size() > 0) {
                        for (ArticleEntity headlineArticle : headlineArticles) {
                            for (ArticleEntity savedArticle : savedArticles) {
                                if (savedArticle.getUrl().equals(headlineArticle.getUrl())) {
                                    headlineArticle.setSaved(true);
                                    break;
                                } else {
                                    headlineArticle.setSaved(false);
                                }
                            }
                        }
                        topHeadlinesLiveData.postValue(headlineArticles);
                    }
                }, throwable -> {
                    Timber.e(throwable, "Error while fetching bookmarked articles:");
                }));
    }

    public LiveData<List<ArticleEntity>> getTopHeadlinesLiveData() {
        return topHeadlinesLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void resetError() {
        errorLiveData.setValue(null);
    }

    public void getTopHeadlines() {
        compositeDisposable.add(
                newsRepository.getTopHeadlines()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(articleEntities -> {
                            topHeadlinesLiveData.setValue(articleEntities);
                        }, throwable -> {
                            Timber.e(throwable, "Error while fetching top headlines");
                            errorLiveData.setValue(Utils.getErrorMessage(throwable));
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
                            errorLiveData.setValue(Utils.getErrorMessage(throwable));
                        })
        );
    }

    public void saveArticle(ArticleEntity articleEntity) {
        compositeDisposable.add(newsRepository.bookmarkArticle(SavedArticleEntity.fromArticleEntity(articleEntity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Article saved");
                }, throwable -> {
                    Timber.e(throwable, "Error while saving article");
                    errorLiveData.setValue(Utils.getErrorMessage(throwable));
                })
        );
    }

    public void deleteArticle(String url) {
        compositeDisposable.add(newsRepository.removeBookmarkedArticle(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Article deleted");
                }, throwable -> {
                    Timber.e(throwable, "Error while deleting article");
                    errorLiveData.setValue(Utils.getErrorMessage(throwable));
                })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

}
