package com.ccojocea.aanews.common;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.ccojocea.aanews.data.remote.NetworkModule;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.OkHttpClient;

@GlideModule
public class CustomGlideModule extends AppGlideModule {

    @Inject
    @Named(NetworkModule.IMAGES)
    protected OkHttpClient client;

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        App.getAppComponent().inject(this);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }

}
