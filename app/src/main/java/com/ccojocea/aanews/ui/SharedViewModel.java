package com.ccojocea.aanews.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;
import com.ccojocea.aanews.ui.settings.PreferenceHelper;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

// This viewModel is shared between activity and fragments
public class SharedViewModel extends ViewModel {

    protected final NewsRepository newsRepository;
    private PreferenceHelper preferenceHelper;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> bookmarkLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> currentItemData = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> swipeData = new MutableLiveData<>();

    public SharedViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
        preferenceHelper = App.getAppComponent().preferenceHelper();
        listenToPreferenceChanges();
    }

    private void listenToPreferenceChanges() {
        compositeDisposable.add(
                preferenceHelper.listenToChanges()
                        .subscribeOn(Schedulers.io())
                        .subscribe(swipeData::postValue));
    }

    public LiveData<Integer> getCurrentItemData() {
        return currentItemData;
    }

    public LiveData<Boolean> getSwipeData() {
        return swipeData;
    }

    public void setCurrentItem(int currentItem) {
        Timber.d("Setting currentItem to: %d", currentItem);
        currentItemData.postValue(currentItem);
    }

    public void setInitialSwipe(boolean isSwipeOn) {
        Timber.d("Preference Debug - setInitialSwipe()");
        preferenceHelper.setNewSwipeValue(isSwipeOn);
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

    public LiveData<Boolean> getBookmarkData() { return bookmarkLiveData; }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void resetError() {
        errorLiveData.setValue(null);
    }

    public void resetBookmarkData() { bookmarkLiveData.setValue(null);}

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public void onBookmarkClicked(ArticleEntity articleEntity, boolean shouldSave) {
        if (articleEntity != null) {
            if (shouldSave) {
                bookmarkArticle(articleEntity);
            } else {
                removeBookmarkedArticle(articleEntity.getUrl());
            }
        }
    }

}
