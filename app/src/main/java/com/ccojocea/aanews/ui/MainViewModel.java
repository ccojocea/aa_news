package com.ccojocea.aanews.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class MainViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<Integer> currentItemData;

    public MainViewModel() {
        currentItemData = new MutableLiveData<>(0);
    }

    public LiveData<Integer> getCurrentItemData() {
        return currentItemData;
    }

    public void setCurrentItem(int currentItem) {
        Timber.d("Setting currentItem to: %d", currentItem);
        currentItemData.postValue(currentItem);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

}
