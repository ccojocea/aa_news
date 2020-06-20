package com.ccojocea.aanews.ui.mynews;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.databinding.FragmentMyNewsBinding;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;
import com.ccojocea.aanews.ui.common.VerticalItemDecoration;

public class MyNewsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, SharedRecyclerViewAdapter.NewsAdapterListener {

    private FragmentMyNewsBinding binding;
    private NewsViewModel viewModel;
    private SharedRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init ViewModel - use requireActivity() so this is bound to the activity (otherwise it would be recreated on config changes)
        viewModel = new ViewModelProvider(requireActivity()).get(NewsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupLiveData();

        adapter = new SharedRecyclerViewAdapter();
        adapter.setAdapterListener(this);
        if (binding.recyclerView.getItemAnimator() != null) {
            binding.recyclerView.getItemAnimator().setChangeDuration(0);
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new VerticalItemDecoration());
        binding.recyclerView.setAdapter(adapter);
        binding.refreshLayout.setOnRefreshListener(this);
    }

    private void setupLiveData() {
        viewModel.getArticlesLiveData().observe(getViewLifecycleOwner(), articleEntities -> {
            if (getContext() != null) {
                binding.refreshLayout.setRefreshing(false);
                adapter.setItems(articleEntities);
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), isError -> {
            if (isError != null && isError) {
                binding.refreshLayout.setRefreshing(false);
                viewModel.resetError();
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.error_please_try_again, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public String getFragmentTitle(Context context) {
        return context.getString(R.string.tab_my_news);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onRefresh() {
        viewModel.refreshData();
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
