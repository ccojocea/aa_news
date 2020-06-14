package com.ccojocea.aanews;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ccojocea.aanews.bookmarks.BookmarksFragment;
import com.ccojocea.aanews.common.BaseFragment;
import com.ccojocea.aanews.localnews.HeadlinesFragment;
import com.ccojocea.aanews.mynews.MyNewsFragment;
import com.ccojocea.aanews.search.SearchFragment;

import java.util.ArrayList;
import java.util.List;

public class MainFragmentStateAdapter extends FragmentStateAdapter {

    private final List<BaseFragment> fragments = new ArrayList<>();

    public MainFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void addFragment(@NonNull FragmentType fragmentType, @Nullable Bundle args) {
        BaseFragment fragment = null;
        switch (fragmentType) {
            case MY_NEWS:
                fragment = new MyNewsFragment();
                break;
            case HEADLINES:
                fragment = new HeadlinesFragment();
                break;
            case BOOKMARKS:
                fragment = new BookmarksFragment();
                break;
            case SEARCH:
                fragment = new SearchFragment();
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

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    public String getPageTitle(Context context, int position) {
        return fragments.get(position).getFragmentTitle(context);
    }

    public enum FragmentType {
        MY_NEWS(0),
        HEADLINES(1),
        BOOKMARKS(2),
        SEARCH(3);

        public int value;

        FragmentType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Nullable
        public FragmentType getType(int value) {
            for (FragmentType fragmentType : values()) {
                if (fragmentType.value == value) {
                    return fragmentType;
                }
            }
            return null;
        }
    }

}
