package com.ccojocea.aanews.common;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.ccojocea.aanews.BuildConfig;
import com.ccojocea.aanews.data.local.AppDatabase;

import java.util.Locale;

import timber.log.Timber;

public class App extends Application implements LifecycleObserver {

    private static App instance;
    private boolean inForeground;
    private Locale locale;

//    @Component(modules = NetworkModule.class)
//    public interface ApplicationComponent{}

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

//        ApplicationComponent applicationComponent = DaggerApp_ApplicationComponent.create();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        locale = new Locale("ro", "RO");
        AppDatabase.initialize(this);
    }

    public Locale getLocale() {
        return locale;
    }

    public static App getApp() {
        return instance;
    }

    public boolean isInForeground() {
        return inForeground;
    }

    // app in foreground
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onStart() {
        inForeground = true;
    }

    // app in background
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onStop() {
        inForeground = false;
    }

}
