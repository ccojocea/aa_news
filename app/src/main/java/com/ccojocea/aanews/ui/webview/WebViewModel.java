package com.ccojocea.aanews.ui.webview;

import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.NewsHelper;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class WebViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private NewsRepository newsRepository;

    public WebViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
    }

    public void bookmarkArticle() {
        ArticleEntity articleEntity = NewsHelper.getInstance().getArticleEntity();
        if (articleEntity != null) {
            compositeDisposable.add(newsRepository.bookmarkArticle(SavedArticleEntity.fromArticleEntity(articleEntity))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        Timber.d("Article saved");
                    }, throwable -> {
                        Timber.e(throwable, "Error while saving article");
                    })
            );
        }
    }

    public void removeBookmarkedArticle() {
        ArticleEntity articleEntity = NewsHelper.getInstance().getArticleEntity();
        if (articleEntity != null) {
            compositeDisposable.add(newsRepository.removeBookmarkedArticle(articleEntity.getUrl())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        Timber.d("Article deleted");
                    }, throwable -> {
                        Timber.e(throwable, "Error while deleting article");
                    })
            );
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

}
