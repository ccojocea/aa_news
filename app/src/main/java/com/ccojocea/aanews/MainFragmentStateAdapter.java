package com.ccojocea.aanews;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ccojocea.aanews.bookmarks.BookmarksFragment;
import com.ccojocea.aanews.common.BaseFragment;
import com.ccojocea.aanews.localnews.LocalFragment;
import com.ccojocea.aanews.mynews.MyNewsFragment;
import com.ccojocea.aanews.search.SearchFragment;

import java.util.ArrayList;
import java.util.List;

public class MainFragmentStateAdapter extends FragmentStateAdapter {

    private List<BaseFragment> fragments = new ArrayList<>();

    public MainFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void addFragment(@NonNull FragmentType fragmentType, @Nullable Bundle args) {
        BaseFragment fragment = null;
        switch (fragmentType) {
            case LOCAL:
                fragment = new LocalFragment();
                break;
            case BOOKMARKS:
                fragment = new BookmarksFragment();
                break;
            case SEARCH:
                fragment = new SearchFragment();
                break;
            case MY_NEWS:
                fragment = new MyNewsFragment();
                break;
        }
        if (args != null) {
            fragment.setArguments(args);
        }
        fragments.add(fragment);
    }

    public void removeFragment(int position) {
        if (fragments.size() > position) {
            fragments.remove(position);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    @NonNull
    public String getPageTitle(int position) {
        return fragments.get(position).getFragmentTitle();
    }

    public enum FragmentType {
        LOCAL,
        MY_NEWS,
        BOOKMARKS,
        SEARCH
    }

}
