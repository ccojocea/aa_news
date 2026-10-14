package com.ccojocea.aanews.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.databinding.ActivityMainBinding;
import com.ccojocea.aanews.ui.common.BaseActivity;
import com.ccojocea.aanews.ui.common.HorizontalFlipTransformation;
import com.ccojocea.aanews.ui.settings.SettingsActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import timber.log.Timber;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private MainFragmentStateAdapter adapter;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot(), binding.toolbar, binding.tabLayout);
        setSupportActionBar(binding.toolbar);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        setupTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean shouldConfirmExit() {
        return true;
    }

    private void setupTabs() {
        adapter = new MainFragmentStateAdapter(this);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.MY_NEWS, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.HEADLINES, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.BOOKMARKS, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.SEARCH, null);
        binding.viewPager2.setPageTransformer(new HorizontalFlipTransformation());
        binding.viewPager2.setAdapter(adapter);
        int limit = (adapter.getItemCount() > 1 ? adapter.getItemCount() - 1 : ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        binding.viewPager2.setOffscreenPageLimit(limit);

        final TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager2, true, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.tab_my_news).toUpperCase());
                    tab.setIcon(R.drawable.ic_home);
                    break;
                case 1:
                    tab.setText(getString(R.string.tab_headlines).toUpperCase());
                    tab.setIcon(R.drawable.ic_headlines);
                    break;
                case 2:
                    tab.setText(getString(R.string.tab_bookmarks).toUpperCase());
                    tab.setIcon(R.drawable.ic_bookmark);
                    break;
                case 3:
                    tab.setText(getString(R.string.tab_search).toUpperCase());
                    tab.setIcon(R.drawable.ic_search);
                    break;
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

        sharedViewModel.getCurrentItemData().observe(this, integer -> {
            if (integer != null) {
                if (integer != binding.viewPager2.getCurrentItem()) {
                    binding.viewPager2.setCurrentItem(integer);
                }
            }
        });

        setupSwipeBehaviour();
    }

    private void setupSwipeBehaviour() {
        Timber.d("Swipe setupSwipeBehaviour");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSwipeOn = preferences.getBoolean(getString(R.string.preference_key_swipe), false);

        sharedViewModel.getSwipeData().observe(this, isSwipeOnUpdate -> {
            Timber.d("Preference - Swipe Data - Main");
            binding.viewPager2.setUserInputEnabled(isSwipeOnUpdate);
        });
        sharedViewModel.setInitialSwipe(isSwipeOn);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedViewModel.setCurrentItem(binding.viewPager2.getCurrentItem());
    }

    @Override
    public View getRoot() {
        return binding.viewPagerLayout; //returning binding.getRoot() would place the popup on top of tabs
    }

}
