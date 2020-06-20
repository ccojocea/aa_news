package com.ccojocea.aanews.ui.search;

import androidx.lifecycle.ViewModel;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.NewsRepository;

public class SearchViewModel extends ViewModel {

    protected final NewsRepository newsRepository;

    public SearchViewModel() {
        newsRepository = App.getAppComponent().newsRepository();
    }

}
