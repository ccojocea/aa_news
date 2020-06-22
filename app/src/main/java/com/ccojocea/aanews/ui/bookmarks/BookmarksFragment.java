package com.ccojocea.aanews.ui.bookmarks;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.databinding.FragmentBookmarksBinding;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.ui.SharedViewModel;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;
import com.ccojocea.aanews.ui.common.VerticalItemDecoration;

import timber.log.Timber;

public class BookmarksFragment extends BaseFragment implements SharedRecyclerViewAdapter.NewsAdapterListener {

    private FragmentBookmarksBinding binding;
    private BookmarksViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private SharedRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(BookmarksViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
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
        adapter.setAdapterListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new VerticalItemDecoration());
        binding.recyclerView.setAdapter(adapter);

        viewModel.getArticleData().observe(getViewLifecycleOwner(), savedArticleEntities -> {
            if (savedArticleEntities.size() == 0) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.emptyView.setVisibility(View.VISIBLE);
            } else {
                binding.emptyView.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                adapter.setChildItems(savedArticleEntities);
            }
        });

        sharedViewModel.getSwipeData().observe(getViewLifecycleOwner(), isSwipeEnabled -> {
            Timber.d("Preference - Swipe Data - Bookmarks");
            adapter.setItemViewSwipeEnabled(!isSwipeEnabled);
        });

        if (getContext() != null) {
            adapter.setupSwipeCallback(binding.recyclerView, getContext());
        }
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

    @Override
    public void onBookmarkClicked(int position, String url, boolean shouldSave) {
        //TODO Cleanup and move entire logic to ViewModel (send article directly)
        if (shouldSave) {
            ArticleEntity articleEntity = adapter.getItem(position);
            if (articleEntity != null) {
                viewModel.bookmarkArticle(articleEntity);
            }
        } else {
            viewModel.removeBookmarkedArticle(url);
        }
    }

}
