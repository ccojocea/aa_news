package com.ccojocea.aanews.ui.bookmarks;

import com.ccojocea.aanews.ui.common.ArticlesViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class BookmarksViewModel extends ArticlesViewModel {

    public BookmarksViewModel() {
        compositeDisposable.add(newsRepository.listenToBookmarks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setArticles,
                        throwable -> onError(throwable, "Error while fetching bookmarked articles"))
        );
    }

}
