package com.ccojocea.aanews.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.common.Event;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;
import com.ccojocea.aanews.ui.settings.PreferenceHelper;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

// This viewModel is shared between activity and fragments
public class SharedViewModel extends ViewModel {

    protected final NewsRepository newsRepository;
    private final PreferenceHelper preferenceHelper;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<Event<String>> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<BookmarkChange>> bookmarkLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> swipeData = new MutableLiveData<>();

    public SharedViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
        preferenceHelper = App.getAppComponent().preferenceHelper();
        listenToPreferenceChanges();
    }

    private void listenToPreferenceChanges() {
        // the current value is emitted right away, then every change made in the settings
        compositeDisposable.add(
                preferenceHelper.listenToViewPagerSwipe()
                        .subscribe(swipeData::setValue,
                                throwable -> Timber.e(throwable, "Error while listening to the swipe preference")));
    }

    /**
     * True when swiping switches between tabs, false when swiping an article shares / bookmarks it.
     */
    public LiveData<Boolean> getSwipeData() {
        return swipeData;
    }

    public LiveData<Event<BookmarkChange>> getBookmarkData() {
        return bookmarkLiveData;
    }

    public LiveData<Event<String>> getErrorLiveData() {
        return errorLiveData;
    }

    public void onBookmarkClicked(@NonNull ArticleEntity articleEntity, boolean shouldSave) {
        changeBookmark(articleEntity, shouldSave, true);
    }

    public void undo(@NonNull BookmarkChange change) {
        changeBookmark(change.article, !change.isSaved, false);
    }

    private void changeBookmark(@NonNull ArticleEntity articleEntity, boolean shouldSave, boolean canUndo) {
        Completable change = shouldSave
                ? newsRepository.bookmarkArticle(SavedArticleEntity.fromArticleEntity(articleEntity))
                : newsRepository.removeBookmarkedArticle(articleEntity.getUrl());
        compositeDisposable.add(change
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> bookmarkLiveData.setValue(new Event<>(new BookmarkChange(articleEntity, shouldSave, canUndo))),
                        throwable -> {
                            Timber.e(throwable, "Error while changing bookmark");
                            errorLiveData.setValue(new Event<>(Utils.getErrorMessage(throwable)));
                        })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public static class BookmarkChange {

        @NonNull
        public final ArticleEntity article;
        public final boolean isSaved;
        public final boolean canUndo;

        BookmarkChange(@NonNull ArticleEntity article, boolean isSaved, boolean canUndo) {
            this.article = article;
            this.isSaved = isSaved;
            this.canUndo = canUndo;
        }

    }

}
