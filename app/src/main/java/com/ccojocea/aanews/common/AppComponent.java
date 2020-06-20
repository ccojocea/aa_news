package com.ccojocea.aanews.common;

import com.ccojocea.aanews.data.NewsRepository;
import com.ccojocea.aanews.data.remote.NetworkModule;
import com.ccojocea.aanews.data.remote.NewsWebService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {NetworkModule.class})
public interface AppComponent {

    NewsRepository newsRepository();

    void inject(CustomGlideModule customGlideModule);

}