package com.ccojocea.aanews.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.ui.settings.PreferenceHelper;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

// This viewModel is shared between activity and fragments
public class SharedViewModel extends ViewModel {

    private PreferenceHelper preferenceHelper;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<Boolean> swipeData = new MutableLiveData<>();

    public SharedViewModel() {
        preferenceHelper = App.getAppComponent().preferenceHelper();
        listenToPreferenceChanges();
    }

    private void listenToPreferenceChanges() {
        compositeDisposable.add(
                preferenceHelper.listenToChanges()
                        .subscribeOn(Schedulers.io())
                        .subscribe(swipeData::postValue));
    }

    public LiveData<Boolean> getSwipeData() {
        return swipeData;
    }

    public void setInitialSwipe(boolean isSwipeOn) {
        Timber.d("Preference Debug - setInitialSwipe()");
        preferenceHelper.setNewSwipeValue(isSwipeOn);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
