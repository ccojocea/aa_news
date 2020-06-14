package com.ccojocea.aanews;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.ccojocea.aanews.common.BaseActivity;
import com.ccojocea.aanews.databinding.ActivityMainBinding;
import com.ccojocea.aanews.settings.SettingsActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private MainFragmentStateAdapter adapter;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no longer needed as app will only follow system setting based on user preference
        // setupDarkModeReceiver();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setupTabs();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getCurrentItemData().observe(this, integer -> {
            if (integer != null) {
                if (integer != binding.viewPager2.getCurrentItem()) {
                    binding.viewPager2.setCurrentItem(integer);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setupTabs() {
        adapter = new MainFragmentStateAdapter(this);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.MY_NEWS, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.HEADLINES, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.BOOKMARKS, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.SEARCH, null);
        binding.viewPager2.setAdapter(adapter);
        int limit = (adapter.getItemCount() > 1 ? adapter.getItemCount() - 1 : ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        binding.viewPager2.setOffscreenPageLimit(limit);

        final TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager2, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(getString(R.string.tab_my_news));
                        tab.setIcon(R.drawable.ic_home);
                        break;
                    case 1:
                        tab.setText(getString(R.string.tab_headlines));
                        tab.setIcon(R.drawable.ic_headlines);
                        break;
                    case 2:
                        tab.setText(getString(R.string.tab_bookmarks));
                        tab.setIcon(R.drawable.ic_bookmark);
                        break;
                    case 3:
                        tab.setText(getString(R.string.tab_search));
                        tab.setIcon(R.drawable.ic_search);
                        break;
                }
            }
        });
        binding.tabLayout.setTabMode(TabLayout.MODE_AUTO);
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayoutMediator.attach();

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (adapter != null) {
                    setTitle(adapter.getPageTitle(MainActivity.this, position));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.setCurrentItem(binding.viewPager2.getCurrentItem());
    }

    private void setupDarkModeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        switch (AppCompatDelegate.getDefaultNightMode()) {
                            case MODE_NIGHT_YES:
                                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                                break;
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        switch (AppCompatDelegate.getDefaultNightMode()) {
                            case MODE_NIGHT_NO:
                                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                                break;
                        }
                }
            }
        }, filter);
    }

}
