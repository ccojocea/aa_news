package com.ccojocea.aanews.ui.mynews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ccojocea.aanews.data.Topics;
import com.ccojocea.aanews.data.remote.enums.NewsLanguage;
import com.ccojocea.aanews.ui.common.ArticlesViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.SerialDisposable;
import timber.log.Timber;

/**
 * Latest articles about the topics the user follows. They are cached in the database, so the last results are shown
 * right away (also offline) while fresh ones are loaded.
 */
public class MyNewsViewModel extends ArticlesViewModel {

    public enum AddTopicResult {
        ADDED,
        INVALID,
        DUPLICATE,
        LIMIT_REACHED
    }

    private final MutableLiveData<List<String>> topicsLiveData = new MutableLiveData<>();
    private final SerialDisposable refreshRequest = new SerialDisposable();

    @NonNull
    private List<String> topics = new ArrayList<>();
    @NonNull
    private NewsLanguage language;

    public MyNewsViewModel() {
        compositeDisposable.add(refreshRequest);
        language = preferenceHelper.getNewsLanguage();

        compositeDisposable.add(newsRepository.listenToMyNews()
                // When the results come back, make sure we switch to main thread to handle them
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setArticles,
                        throwable -> onError(throwable, "Received error while loading cached articles"))
        );

        // emits the saved topics right away (first refresh) and again each time they change
        compositeDisposable.add(preferenceHelper.listenToMyNewsTopics()
                .subscribe(newTopics -> {
                    topics = newTopics;
                    topicsLiveData.setValue(newTopics);
                    refreshData();
                }, throwable -> Timber.e(throwable, "Error while listening to topics"))
        );

        // the current language is already used above, only react to changes
        compositeDisposable.add(preferenceHelper.listenToNewsLanguage()
                .skip(1)
                .subscribe(newLanguage -> {
                    language = newLanguage;
                    refreshData();
                }, throwable -> Timber.e(throwable, "Error while listening to the language preference"))
        );
    }

    public LiveData<List<String>> getTopicsLiveData() {
        return topicsLiveData;
    }

    public boolean hasTopics() {
        return !topics.isEmpty();
    }

    public void refreshData() {
        setLoading(true);
        refreshRequest.set(newsRepository.refreshMyNews(topics, language)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Data fetch completed");
                    setLoading(false);
                }, throwable -> {
                    setLoading(false);
                    onError(throwable, "Received error while fetching articles");
                })
        );
    }

    @NonNull
    public AddTopicResult addTopic(@Nullable String topic) {
        String normalized = Topics.normalize(topic);
        if (normalized == null) {
            return AddTopicResult.INVALID;
        }
        if (Topics.contains(topics, normalized)) {
            return AddTopicResult.DUPLICATE;
        }
        if (topics.size() >= Topics.MAX_TOPICS) {
            return AddTopicResult.LIMIT_REACHED;
        }
        // saving triggers the topics listener, which refreshes the articles
        preferenceHelper.setMyNewsTopics(Topics.add(topics, normalized));
        return AddTopicResult.ADDED;
    }

    public void removeTopic(@NonNull String topic) {
        List<String> updated = new ArrayList<>(topics);
        if (updated.remove(topic)) {
            preferenceHelper.setMyNewsTopics(updated);
        }
    }

}
