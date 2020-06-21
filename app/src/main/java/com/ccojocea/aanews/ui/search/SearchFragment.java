package com.ccojocea.aanews.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.ui.SharedViewModel;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.databinding.FragmentSearchBinding;

import timber.log.Timber;

public class SearchFragment extends BaseFragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private SharedViewModel sharedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO Setup LiveData - viewModel + sharedViewModel

        binding.textView.setText(getClass().getSimpleName());

        sharedViewModel.getSwipeData().observe(getViewLifecycleOwner(), isSwipeEnabled -> {
            Timber.d("Preference - Swipe Data - Search: %s", isSwipeEnabled);
        });

        //TODO When adapter is added
//        if (getContext() != null) {
//            adapter.setupSwipeCallback(binding.recyclerView, getContext());
//        }
    }

    @Override
    public String getFragmentTitle(Context context) {
        return context.getString(R.string.tab_search);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
