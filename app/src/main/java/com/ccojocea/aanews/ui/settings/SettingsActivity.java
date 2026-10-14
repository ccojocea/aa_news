package com.ccojocea.aanews.ui.settings;

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

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.remote.enums.NewsCountry;
import com.ccojocea.aanews.data.remote.enums.NewsLanguage;
import com.ccojocea.aanews.databinding.ActivitySettingsBinding;
import com.ccojocea.aanews.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot(), binding.toolbar, binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle(R.string.settings_title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public View getRoot() {
        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Inject
        PreferenceHelper preferenceHelper;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            App.getAppComponent().inject(this);

            // dark mode listener
            // the other preferences are picked up through PreferenceHelper, which listens to all changes
            Preference uiMode = findPreference(getString(R.string.preference_key_ui_mode));
            if (uiMode != null) {
                uiMode.setOnPreferenceChangeListener(onPreferenceChangeListener);
            }

            setupCountryPreference();
            setupLanguagePreference();
        }

        private void setupCountryPreference() {
            ListPreference country = findPreference(getString(R.string.preference_key_country));
            if (country == null) {
                return;
            }
            List<NewsCountry> countries = new ArrayList<>(Arrays.asList(NewsCountry.values()));
            countries.sort(Comparator.comparing(NewsCountry::getDisplayName));
            CharSequence[] entries = new CharSequence[countries.size()];
            CharSequence[] values = new CharSequence[countries.size()];
            for (int i = 0; i < countries.size(); i++) {
                entries[i] = countries.get(i).getDisplayName();
                values[i] = countries.get(i).getValue();
            }
            country.setEntries(entries);
            country.setEntryValues(values);
            if (country.getValue() == null) {
                // until the user picks one, the headlines use the country of the device
                country.setValue(preferenceHelper.getHeadlinesCountry().getValue());
            }
        }

        private void setupLanguagePreference() {
            ListPreference language = findPreference(getString(R.string.preference_key_language));
            if (language == null) {
                return;
            }
            NewsLanguage[] languages = NewsLanguage.values();
            CharSequence[] entries = new CharSequence[languages.length];
            CharSequence[] values = new CharSequence[languages.length];
            for (int i = 0; i < languages.length; i++) {
                String displayName = languages[i].getDisplayName();
                String value = languages[i].getValue();
                entries[i] = displayName != null ? displayName : getString(R.string.settings_language_all);
                // a ListPreference can't store null, "" stands for all languages
                values[i] = value != null ? value : "";
            }
            language.setEntries(entries);
            language.setEntryValues(values);
            if (language.getValue() == null) {
                language.setValue("");
            }
        }

        private final Preference.OnPreferenceChangeListener onPreferenceChangeListener = (preference, newValue) -> {
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
            return false;
        };

    }

}
