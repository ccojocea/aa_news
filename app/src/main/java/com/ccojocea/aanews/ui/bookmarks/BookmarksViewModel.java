package com.ccojocea.aanews.ui.bookmarks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class BookmarksViewModel extends ViewModel {

    protected final NewsRepository newsRepository;

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> bookmarkLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<List<SavedArticleEntity>> articlesData = new MutableLiveData<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ArticleEntity articleBackup;

    public BookmarksViewModel() {
        newsRepository = App.getAppComponent().newsRepository();

        compositeDisposable.add(newsRepository.listenToBookmarks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(articleEntityList -> {
                    List<SavedArticleEntity> savedArticleEntityList = articleEntityList.stream()
                            .sorted(new Comparator<ArticleEntity>() {
                                @Override
                                public int compare(ArticleEntity o1, ArticleEntity o2) {
                                    return o2.getPublishedAt().compareTo(o1.getPublishedAt());
                                }
                            })
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

    private void bookmarkArticle(ArticleEntity articleEntity) {
        compositeDisposable.add(newsRepository.bookmarkArticle(SavedArticleEntity.fromArticleEntity(articleEntity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    bookmarkLiveData.setValue(true);
                }, throwable -> {
                    Timber.e(throwable, "Error while saving article");
                    errorLiveData.setValue(Utils.getErrorMessage(throwable));
                })
        );
    }

    private void removeBookmarkedArticle(String url) {
        compositeDisposable.add(newsRepository.removeBookmarkedArticle(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    bookmarkLiveData.setValue(false);
                }, throwable -> {
                    Timber.e(throwable, "Error while deleting article");
                    errorLiveData.setValue(Utils.getErrorMessage(throwable));
                })
        );
    }

    public void onBookmarkClicked(ArticleEntity articleEntity, boolean shouldSave) {
        articleBackup = articleEntity;
        if (articleEntity != null) {
            if (shouldSave) {
                bookmarkArticle(articleEntity);
            } else {
                removeBookmarkedArticle(articleEntity.getUrl());
            }
        }
    }

    public LiveData<Boolean> getBookmarkData() { return bookmarkLiveData; }

    public void resetBookmarkData() { bookmarkLiveData.setValue(null);}

    public void restoreBookmark() {
        onBookmarkClicked(articleBackup, true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
