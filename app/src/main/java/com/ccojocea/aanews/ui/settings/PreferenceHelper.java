package com.ccojocea.aanews.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.remote.enums.NewsCategory;
import com.ccojocea.aanews.data.remote.enums.NewsCountry;
import com.ccojocea.aanews.data.remote.enums.NewsLanguage;
import com.ccojocea.aanews.data.remote.enums.NewsSortBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Single place for reading and writing the user's preferences.
 * Changes are pushed through the listenTo* observables, wherever they were made (settings screen or the app).
 */
@Singleton
public class PreferenceHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_HEADLINES_CATEGORY = "key_headlines_category";
    private static final String KEY_SEARCH_SORT_BY = "key_search_sort_by";
    private static final String KEY_MY_NEWS_TOPICS = "key_my_news_topics";

    // topics are stored as one string to keep their order, a string set would not
    private static final String TOPICS_SEPARATOR = "\n";
    private static final List<String> DEFAULT_TOPICS = Collections.singletonList("Android");

    private final Context context;
    private final SharedPreferences preferences;
    private final PublishSubject<String> changedKeys = PublishSubject.create();

    @Inject
    public PreferenceHelper() {
        Timber.d("PreferenceHelper()");
        context = App.getApp();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        // SharedPreferences only keeps a weak reference to the listener, this singleton keeps it alive
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if (key != null) {
            changedKeys.onNext(key);
        }
    }

    /**
     * Emits the current value and then each change of the value of the given key.
     */
    private <T> Observable<T> listenTo(@NonNull String key, @NonNull Callable<T> reader) {
        return changedKeys
                .filter(key::equals)
                .map(changedKey -> reader.call())
                .startWith(Observable.fromCallable(reader))
                .distinctUntilChanged();
    }

    /* Swipe behaviour */

    public boolean isViewPagerSwipeEnabled() {
        return preferences.getBoolean(context.getString(R.string.preference_key_swipe), true);
    }

    public Observable<Boolean> listenToViewPagerSwipe() {
        return listenTo(context.getString(R.string.preference_key_swipe), this::isViewPagerSwipeEnabled);
    }

    /* Headlines */

    /**
     * The country picked in the settings or, until one is picked, the country of the device.
     */
    @NonNull
    public NewsCountry getHeadlinesCountry() {
        NewsCountry country = NewsCountry.fromValue(preferences.getString(context.getString(R.string.preference_key_country), null));
        return country != null ? country : NewsCountry.fromLocale(Locale.getDefault());
    }

    public Observable<NewsCountry> listenToHeadlinesCountry() {
        return listenTo(context.getString(R.string.preference_key_country), this::getHeadlinesCountry);
    }

    @NonNull
    public NewsCategory getHeadlinesCategory() {
        return NewsCategory.fromValue(preferences.getString(KEY_HEADLINES_CATEGORY, null));
    }

    public void setHeadlinesCategory(@NonNull NewsCategory category) {
        preferences.edit().putString(KEY_HEADLINES_CATEGORY, category.getValue()).apply();
    }

    /* My News and search */

    @NonNull
    public NewsLanguage getNewsLanguage() {
        return NewsLanguage.fromValue(preferences.getString(context.getString(R.string.preference_key_language), null));
    }

    public Observable<NewsLanguage> listenToNewsLanguage() {
        return listenTo(context.getString(R.string.preference_key_language), this::getNewsLanguage);
    }

    @NonNull
    public NewsSortBy getSearchSortBy() {
        return NewsSortBy.fromValue(preferences.getString(KEY_SEARCH_SORT_BY, null));
    }

    public void setSearchSortBy(@NonNull NewsSortBy sortBy) {
        preferences.edit().putString(KEY_SEARCH_SORT_BY, sortBy.getValue()).apply();
    }

    @NonNull
    public List<String> getMyNewsTopics() {
        if (!preferences.contains(KEY_MY_NEWS_TOPICS)) {
            return new ArrayList<>(DEFAULT_TOPICS);
        }
        String topics = preferences.getString(KEY_MY_NEWS_TOPICS, "");
        if (topics == null || topics.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(topics.split(TOPICS_SEPARATOR)));
    }

    public void setMyNewsTopics(@NonNull List<String> topics) {
        preferences.edit().putString(KEY_MY_NEWS_TOPICS, TextUtils.join(TOPICS_SEPARATOR, topics)).apply();
    }

    public Observable<List<String>> listenToMyNewsTopics() {
        return listenTo(KEY_MY_NEWS_TOPICS, this::getMyNewsTopics);
    }

}
