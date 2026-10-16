package com.ccojocea.aanews.ui.localnews;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ccojocea.aanews.data.remote.enums.NewsCategory;
import com.ccojocea.aanews.data.remote.enums.NewsCountry;
import com.ccojocea.aanews.data.remote.enums.NewsLanguage;
import com.ccojocea.aanews.models.ArticlePage;
import com.ccojocea.aanews.ui.common.PagedArticlesViewModel;

import io.reactivex.Single;
import timber.log.Timber;

/**
 * Either the top headlines of the country (by category) or, since those are missing or poor for many countries,
 * the newest articles about the country in the language picked in the settings.
 */
public class HeadlinesViewModel extends PagedArticlesViewModel {

    private final MutableLiveData<NewsCountry> countryLiveData = new MutableLiveData<>();

    @NonNull
    private NewsCountry country;
    @NonNull
    private NewsCategory category;
    @NonNull
    private NewsLanguage language;
    private boolean isAboutCountry;

    public HeadlinesViewModel() {
        country = preferenceHelper.getHeadlinesCountry();
        category = preferenceHelper.getHeadlinesCategory();
        language = preferenceHelper.getNewsLanguage();
        isAboutCountry = preferenceHelper.isHeadlinesAboutCountry();

        // emits the current country right away (first load) and again when it is changed in the settings
        compositeDisposable.add(preferenceHelper.listenToHeadlinesCountry()
                .subscribe(newCountry -> {
                    country = newCountry;
                    countryLiveData.setValue(newCountry);
                    reload();
                }, throwable -> Timber.e(throwable, "Error while listening to the country preference"))
        );

        // the language only matters for the news about the country, top headlines are in the language of the country
        compositeDisposable.add(preferenceHelper.listenToNewsLanguage()
                .skip(1)
                .subscribe(newLanguage -> {
                    language = newLanguage;
                    if (isAboutCountry) {
                        reload();
                    }
                }, throwable -> Timber.e(throwable, "Error while listening to the language preference"))
        );
    }

    public LiveData<NewsCountry> getCountryLiveData() {
        return countryLiveData;
    }

    @NonNull
    public NewsCountry getCountry() {
        return country;
    }

    @NonNull
    public NewsCategory getCategory() {
        return category;
    }

    public boolean isAboutCountry() {
        return isAboutCountry;
    }

    public void showAboutCountry() {
        if (isAboutCountry) {
            return;
        }
        isAboutCountry = true;
        preferenceHelper.setHeadlinesAboutCountry(true);
        reload();
    }

    public void setCategory(@NonNull NewsCategory newCategory) {
        if (!isAboutCountry && newCategory == category) {
            return;
        }
        isAboutCountry = false;
        category = newCategory;
        preferenceHelper.setHeadlinesAboutCountry(false);
        preferenceHelper.setHeadlinesCategory(newCategory);
        reload();
    }

    @NonNull
    @Override
    protected Single<ArticlePage> loadPage(int page) {
        if (isAboutCountry) {
            return newsRepository.getNewsAboutCountry(country, language, page);
        }
        return newsRepository.getTopHeadlines(country, category, page);
    }

}
