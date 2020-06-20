package com.ccojocea.aanews.ui.bookmarks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.NewsRepository;
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

        //TODO Delete if not needed
//        compositeDisposable.add(NewsRepository.getInstance().getBookmarks())
    }

    public LiveData<List<SavedArticleEntity>> getArticleData() {
        return articlesData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
