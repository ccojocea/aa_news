package com.ccojocea.aanews.ui.webview;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class WebViewModel extends ViewModel {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final NewsRepository newsRepository;

    private final MutableLiveData<Boolean> savedData = new MutableLiveData<>(false);

    private ArticleEntity article;

    public WebViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
    }

    public void setArticle(@NonNull ArticleEntity article) {
        if (this.article != null) {
            return;
        }
        this.article = article;
        savedData.setValue(article.isSaved());
        compositeDisposable.add(newsRepository.listenToBookmarkedUrls()
                .map(urls -> urls.contains(article.getUrl()))
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(savedData::setValue,
                        throwable -> Timber.e(throwable, "Error while listening to bookmarks"))
        );
    }

    public LiveData<Boolean> getSavedData() {
        return savedData;
    }

    public boolean isSaved() {
        return Boolean.TRUE.equals(savedData.getValue());
    }

    public void toggleBookmark() {
        if (article == null) {
            return;
        }
        if (isSaved()) {
            compositeDisposable.add(newsRepository.removeBookmarkedArticle(article.getUrl())
                    .subscribe(() -> Timber.d("Article deleted"),
                            throwable -> Timber.e(throwable, "Error while deleting article"))
            );
        } else {
            compositeDisposable.add(newsRepository.bookmarkArticle(SavedArticleEntity.fromArticleEntity(article))
                    .subscribe(() -> Timber.d("Article saved"),
                            throwable -> Timber.e(throwable, "Error while saving article"))
            );
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

}
