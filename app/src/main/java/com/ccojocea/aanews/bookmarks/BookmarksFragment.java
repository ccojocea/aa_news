package com.ccojocea.aanews.bookmarks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.NewsViewModel;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.BaseFragment;
import com.ccojocea.aanews.databinding.LayoutBookmarksFragmentBinding;

public class BookmarksFragment extends BaseFragment {

    private LayoutBookmarksFragmentBinding binding;
    private NewsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init ViewModel - use requireActivity() so this is bound to the activity (otherwise it would be recreated on config changes)
        viewModel = new ViewModelProvider(requireActivity()).get(NewsViewModel.class);
        setFragmentTitle(getString(R.string.tab_bookmarks));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutBookmarksFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO setup LiveData
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
