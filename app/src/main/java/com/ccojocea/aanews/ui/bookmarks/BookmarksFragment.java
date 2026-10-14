package com.ccojocea.aanews.ui.bookmarks;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.databinding.FragmentBookmarksBinding;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;

public class BookmarksFragment extends BaseFragment {

    private FragmentBookmarksBinding binding;
    private BookmarksViewModel viewModel;
    private SharedRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(BookmarksViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookmarksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SharedRecyclerViewAdapter();
        adapter.setDefaultType(true);
        // removing a bookmark shows a snackbar with undo, see MainActivity
        setupArticleList(binding.recyclerView, adapter);

        viewModel.getArticlesLiveData().observe(getViewLifecycleOwner(), savedArticleEntities -> {
            adapter.submitList(savedArticleEntities);
            boolean isEmpty = savedArticleEntities.isEmpty();
            binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), this::showError);
    }

    @Override
    public String getFragmentTitle(Context context) {
        return context.getString(R.string.tab_bookmarks);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
