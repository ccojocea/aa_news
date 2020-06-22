package com.ccojocea.aanews.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.internal.Util;
import timber.log.Timber;

public class SearchViewModel extends ViewModel {

    private static final int DELAY = 500;

    protected final NewsRepository newsRepository;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<List<ArticleEntity>> resultsData = new MutableLiveData<>();

    public SearchViewModel() {
        newsRepository = App.getAppComponent().newsRepository();

        // update the list if there's a change in the bookmark database
        compositeDisposable.add(newsRepository.listenToBookmarks()
                //keep the work in background due to iterations, update with postValue
                .observeOn(Schedulers.io())
                .subscribe(savedArticles -> {
                    List<ArticleEntity> resultArticles = resultsData.getValue();
                    if (resultArticles != null && resultArticles.size() > 0) {
                        for (ArticleEntity resultArticle : resultArticles) {
                            for (ArticleEntity savedArticle : savedArticles) {
                                if (savedArticle.getUrl().equals(resultArticle.getUrl())) {
                                    resultArticle.setSaved(true);
                                    break;
                                } else {
                                    resultArticle.setSaved(false);
                                }
                            }
                        }
                        resultsData.postValue(resultArticles);
                    }
                }, throwable -> {
                    Timber.e(throwable, "Error while fetching bookmarked articles:");
                }));
    }

    public void searchForArticle(String query) {
        compositeDisposable.add(newsRepository.searchArticles(query, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(articleEntityList -> {
                    resultsData.setValue(articleEntityList);
                }, throwable -> {
                    errorLiveData.setValue(Utils.getErrorMessage(throwable));
                    Timber.e(throwable);
                }));
    }

    public void bookmarkArticle(ArticleEntity articleEntity) {
        compositeDisposable.add(newsRepository.bookmarkArticle(SavedArticleEntity.fromArticleEntity(articleEntity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Article saved");
                }, throwable -> {
                    Timber.e(throwable, "Error while saving article");
                })
        );
    }

    public void removeBookmarkedArticle(String url) {
        compositeDisposable.add(newsRepository.removeBookmarkedArticle(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Article deleted");
                }, throwable -> {
                    Timber.e(throwable, "Error while deleting article");
                })
        );
    }

    public MutableLiveData<List<ArticleEntity>> getResultsData() {
        return resultsData;
    }

    public void setupSearchObserver(Observable<String> observableQuery) {
        // Subscribe an Observer
        observableQuery
                .debounce(DELAY, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(String s) {
                        if (!s.isEmpty()) {
                            searchForArticle(s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
        compositeDisposable.clear();
    }

}
