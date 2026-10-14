package com.ccojocea.aanews.common;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.ccojocea.aanews.BuildConfig;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.data.local.AppDatabase;

import timber.log.Timber;

public class App extends Application {

    private static App instance;

    private AppComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Reference to the application graph/component that is used across the whole app
        applicationComponent = DaggerAppComponent.create();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // make sure the defaults declared in preferences.xml are available before the settings screen is opened
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        AppDatabase.initialize(this);
        setupDisplayMode();
    }

    public static App getApp() {
        return instance;
    }

    public static AppComponent getAppComponent() {
        return instance.applicationComponent;
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

}
