package com.ccojocea.aanews.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.ui.common.BaseActivity;
import com.ccojocea.aanews.databinding.ActivitySettingsBinding;

import javax.inject.Inject;

import io.reactivex.Single;
import timber.log.Timber;

public class SettingsActivity extends BaseActivity {

    private static final String KEY_UI_MODE = "key_pref_ui_mode";
    private static final String KEY_SWIPE = "key_swipe";

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle(R.string.settings_title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public View getRoot() {
        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressedOverride();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Inject
        PreferenceHelper preferenceHelper;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            App.getAppComponent().inject(this);

            // dark mode listener
            Preference uiMode = findPreference(getString(R.string.preference_key_ui_mode));
            if (uiMode != null) {
                uiMode.setOnPreferenceChangeListener(onPreferenceChangeListener);
            }

            // swipe listener
            Preference swipe = findPreference(getString(R.string.preference_key_swipe));
            if (swipe != null) {
                swipe.setOnPreferenceChangeListener(onPreferenceChangeListener);
            }

        }

        private Preference.OnPreferenceChangeListener onPreferenceChangeListener = (preference, newValue) -> {
            if (preference instanceof ListPreference) {
                String newValueString = newValue.toString();
                if (newValueString.equals(getString(R.string.settings_system_mode_display))) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else if (newValueString.equals(getString(R.string.settings_light_mode_display))) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                return true;
            }

            if (preference instanceof SwitchPreference) {
                if (preference.getKey().equals(getString(R.string.preference_key_swipe))) {
                    Timber.d("Preferences debug - key: %s", newValue);
                    preferenceHelper.setNewSwipeValue((Boolean)newValue);
                    return true;
                }
            }

            return false;
        };

    }

}
