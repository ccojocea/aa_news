package com.ccojocea.aanews.ui.bookmarks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class BookmarksViewModel extends ViewModel {

    protected final NewsRepository newsRepository;

    private MutableLiveData<List<SavedArticleEntity>> articlesData;
    private final CompositeDisposable compositeDisposable;

    public BookmarksViewModel() {
        newsRepository = App.getAppComponent().newsRepository();

        articlesData = new MutableLiveData<>();
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(newsRepository.listenToBookmarks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(articleEntityList -> {
                    List<SavedArticleEntity> savedArticleEntityList = articleEntityList.stream()
                            .map(articleEntity -> SavedArticleEntity.fromArticleEntity(articleEntity))
                            .collect(Collectors.toList());
                    articlesData.setValue(savedArticleEntityList);
                }, throwable -> {
                    Timber.e(throwable, "Error while fetching bookmarked articles:");
                })
        );
    }

    public LiveData<List<SavedArticleEntity>> getArticleData() {
        return articlesData;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
