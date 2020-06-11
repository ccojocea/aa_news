package com.ccojocea.aanews.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.BaseFragment;
import com.ccojocea.aanews.databinding.LayoutSearchFragmentBinding;

public class SearchFragment extends BaseFragment {

    private LayoutSearchFragmentBinding binding;
    private SearchViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init ViewModel - use requireActivity() so this is bound to the activity (otherwise it would be recreated on config changes)
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        setFragmentTitle(getString(R.string.tab_search));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutSearchFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO Setup LiveData
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
