package com.ccojocea.aanews.common;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;

import com.ccojocea.aanews.BuildConfig;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.data.local.AppDatabase;

import java.util.Locale;

import timber.log.Timber;

public class App extends Application implements LifecycleObserver {

    private static App instance;
    private boolean inForeground;
    private Locale locale;

    private AppComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Reference to the application graph/component that is used across the whole app
        applicationComponent = DaggerAppComponent.create();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        //TODO
        locale = new Locale("ro", "RO");

        AppDatabase.initialize(this);
        setupDisplayMode();
    }

    public Locale getLocale() {
        return locale;
    }

    public static App getApp() {
        return instance;
    }

    public static AppComponent getAppComponent() {
        return instance.applicationComponent;
    }

    public boolean isInForeground() {
        return inForeground;
    }

    private void setupDisplayMode() {
        // get preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mode = sharedPreferences.getString(getString(R.string.preference_key_ui_mode), getString(R.string.settings_system_mode_display));
        if (mode.equals(getString(R.string.settings_system_mode_display))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (mode.equals(getString(R.string.settings_light_mode_display))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
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
