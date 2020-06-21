package com.ccojocea.aanews.common;

import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.data.remote.NetworkModule;
import com.ccojocea.aanews.data.remote.NewsWebService;
import com.ccojocea.aanews.ui.MainActivity;
import com.ccojocea.aanews.ui.settings.PreferenceHelper;
import com.ccojocea.aanews.ui.settings.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {NetworkModule.class})
public interface AppComponent {

    NewsRepository newsRepository();

    PreferenceHelper preferenceHelper();

    void inject(CustomGlideModule customGlideModule);

    void inject(SettingsActivity.SettingsFragment settingsFragment);

}