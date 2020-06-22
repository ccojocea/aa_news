package com.ccojocea.aanews.ui.localnews;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.ui.MainActivity;
import com.ccojocea.aanews.ui.SharedViewModel;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;
import com.ccojocea.aanews.ui.common.VerticalItemDecoration;
import com.ccojocea.aanews.databinding.FragmentHeadlinesBinding;
import com.google.android.material.snackbar.Snackbar;

import timber.log.Timber;

public class HeadlinesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, SharedRecyclerViewAdapter.NewsAdapterListener {

    private FragmentHeadlinesBinding binding;
    private HeadlinesViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private SharedRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HeadlinesViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHeadlinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SharedRecyclerViewAdapter();
        adapter.setAdapterListener(this);
        if (binding.recyclerView.getItemAnimator() != null) {
            binding.recyclerView.getItemAnimator().setChangeDuration(0);
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new VerticalItemDecoration());
        binding.recyclerView.setAdapter(adapter);
        binding.refreshLayout.setOnRefreshListener(this);

        setupLiveData();

        if (getContext() != null) {
            adapter.setupSwipeCallback(binding.recyclerView, getContext());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public String getFragmentTitle(Context context) {
        return context.getString(R.string.title_headlines);
    }

    public void setupLiveData() {
        viewModel.getTopHeadlinesLiveData().observe(getViewLifecycleOwner(), articleEntities -> {
            if (getContext() != null) {
                binding.refreshLayout.setRefreshing(false);
                if (articleEntities.size() > 0) {
                    adapter.setItems(articleEntities);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                } else {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                binding.refreshLayout.setRefreshing(false);
                viewModel.resetError();
                if (getContext() != null) {
                    Utils.showSnackBar(((MainActivity)getContext()).getRoot(), errorMessage, Snackbar.LENGTH_LONG, Gravity.CENTER_HORIZONTAL);
                }
            }
        });

        sharedViewModel.getSwipeData().observe(getViewLifecycleOwner(), isViewPagerSwipeEnabled -> {
            Timber.d("Preference - Swipe Data - Headlines");
            adapter.setItemViewSwipeEnabled(!isViewPagerSwipeEnabled);
        });
    }

    @Override
    public void onRefresh() {
        viewModel.getTopHeadlines();
    }

    @Override
    public void onBookmarkClicked(int position, String url, boolean shouldSave) {
        if (shouldSave) {
            ArticleEntity articleEntity = adapter.getItem(position);
            if (articleEntity != null) {
                viewModel.saveArticle(articleEntity);
            }
        } else {
            viewModel.deleteArticle(url);
        }
    }

}
