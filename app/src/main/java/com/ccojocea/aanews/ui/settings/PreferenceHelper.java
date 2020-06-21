package com.ccojocea.aanews.ui.settings;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

@Singleton
public class PreferenceHelper {

    private PublishSubject<Boolean> subject;

    @Inject
    public PreferenceHelper() {
        Timber.d("PreferenceHelper()");
        subject = PublishSubject.create();
    }

    public void setNewSwipeValue(Boolean newValue) {
        subject.onNext(newValue);
    }

    public Observable<Boolean> listenToChanges() {
        Timber.d("Preference Debug - listenToChanges()");
        return subject.subscribeOn(Schedulers.io());
    }

}
