package com.ccojocea.aanews;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ccojocea.aanews.common.BaseActivity;
import com.ccojocea.aanews.data.local.AppDatabase;
import com.ccojocea.aanews.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wajahatkarim3.roomexplorer.RoomExplorer;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private MainFragmentStateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup dark mode config change receiver
        setupDarkModeReceiver();

        // set binding & content
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        setupTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode not active
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.light_mode));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode active
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.dark_mode));
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_mode:
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Night mode not active - activate
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Night mode active - deactivate
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                        break;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                RoomExplorer.show(this, AppDatabase.class, AppDatabase.DATABASE_NAME);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void setupTabs() {
        adapter = new MainFragmentStateAdapter(this);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.MY_NEWS, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.LOCAL, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.BOOKMARKS, null);
        adapter.addFragment(MainFragmentStateAdapter.FragmentType.SEARCH, null);
        binding.viewPager2.setAdapter(adapter);
        binding.viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        final TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager2, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(getString(R.string.tab_my_news));
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                    case 1:
                        tab.setText(getString(R.string.tab_local_news));
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                    case 2:
                        tab.setText(getString(R.string.tab_bookmarks));
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                    case 3:
                        tab.setText(getString(R.string.tab_search));
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                }
            }
        });
        //        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayoutMediator.attach();

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setTitle(((MainFragmentStateAdapter)binding.viewPager2.getAdapter()).getPageTitle(position));
            }
        });
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
