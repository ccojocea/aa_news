package com.ccojocea.aanews.ui.localnews;

import androidx.annotation.NonNull;

import com.ccojocea.aanews.data.remote.enums.NewsCategory;
import com.ccojocea.aanews.data.remote.enums.NewsCountry;
import com.ccojocea.aanews.models.ArticlePage;
import com.ccojocea.aanews.ui.common.PagedArticlesViewModel;

import io.reactivex.Single;
import timber.log.Timber;

public class HeadlinesViewModel extends PagedArticlesViewModel {

    @NonNull
    private NewsCountry country;
    @NonNull
    private NewsCategory category;

    public HeadlinesViewModel() {
        country = preferenceHelper.getHeadlinesCountry();
        category = preferenceHelper.getHeadlinesCategory();

        // emits the current country right away (first load) and again when it is changed in the settings
        compositeDisposable.add(preferenceHelper.listenToHeadlinesCountry()
                .subscribe(newCountry -> {
                    country = newCountry;
                    reload();
                }, throwable -> Timber.e(throwable, "Error while listening to the country preference"))
        );
    }

    @NonNull
    public NewsCountry getCountry() {
        return country;
    }

    @NonNull
    public NewsCategory getCategory() {
        return category;
    }

    public void setCategory(@NonNull NewsCategory newCategory) {
        if (newCategory == category) {
            return;
        }
        category = newCategory;
        preferenceHelper.setHeadlinesCategory(newCategory);
        reload();
    }

    @NonNull
    @Override
    protected Single<ArticlePage> loadPage(int page) {
        return newsRepository.getTopHeadlines(country, category, page);
    }

}
