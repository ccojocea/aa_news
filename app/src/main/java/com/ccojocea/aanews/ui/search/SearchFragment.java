package com.ccojocea.aanews.ui.search;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.data.remote.enums.NewsSortBy;
import com.ccojocea.aanews.databinding.FragmentSearchBinding;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.ui.common.LoadMoreScrollListener;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;
import com.google.android.material.chip.Chip;

import timber.log.Timber;

public class SearchFragment extends BaseFragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private SharedRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
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

        // disable SearchView underline highlight
        View v = binding.searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (v != null) {
            v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryDarkColor));
        }

        adapter = new SharedRecyclerViewAdapter();
        setupArticleList(binding.recyclerView, adapter);
        binding.recyclerView.addOnScrollListener(new LoadMoreScrollListener(viewModel::loadMore));

        viewModel.getArticlesLiveData().observe(getViewLifecycleOwner(), articleEntityList -> {
            adapter.submitList(articleEntityList);
            boolean isEmpty = articleEntityList.isEmpty();
            binding.recyclerView.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
            binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
            if (isEmpty) {
                binding.emptyText.setText(viewModel.hasQuery()
                        ? getString(R.string.search_no_results, viewModel.getQuery())
                        : getString(R.string.search_description));
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), this::showError);

        setupAnimations();
        setupSortChips();
        setupSearchView();
    }

    private void setupSortChips() {
        LayoutInflater inflater = getLayoutInflater();
        for (NewsSortBy sortBy : NewsSortBy.values()) {
            Chip chip = (Chip) inflater.inflate(R.layout.layout_filter_chip, binding.sortChips, false);
            // chips are created again with new ids together with the view, their state must not be restored
            chip.setId(View.generateViewId());
            chip.setSaveEnabled(false);
            chip.setText(getSortName(sortBy));
            chip.setTag(sortBy);
            binding.sortChips.addView(chip);
            if (sortBy == viewModel.getSortBy()) {
                chip.setChecked(true);
            }
        }
        binding.sortChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            Chip checked = group.findViewById(checkedIds.get(0));
            if (checked != null && checked.getTag() instanceof NewsSortBy) {
                binding.recyclerView.scrollToPosition(0);
                viewModel.setSortBy((NewsSortBy) checked.getTag());
            }
        });
    }

    @StringRes
    private static int getSortName(@NonNull NewsSortBy sortBy) {
        switch (sortBy) {
            case RELEVANCY:
                return R.string.search_sort_relevancy;
            case POPULARITY:
                return R.string.search_sort_popularity;
            case PUBLISHED_AT:
            default:
                return R.string.search_sort_newest;
        }
    }

    private void setupAnimations() {
        ViewTreeObserver viewTreeObserver = binding.searchLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (binding == null) {
                        return;
                    }
                    int padding = binding.searchLayout.getMeasuredHeight();
                    binding.recyclerView.addItemDecoration(new TopItemDecoration(padding));

                    binding.searchLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final float MIN = 150;
            int scrollDist = 0;
            boolean isVisible = true;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (isVisible && scrollDist > MIN) {
                    Timber.d("Hide");
                    hide();
                    scrollDist = 0;
                    isVisible = false;
                } else if (!isVisible && scrollDist < (-MIN / 5)) {
                    Timber.d("Show");
                    show();
                    scrollDist = 0;
                    isVisible = true;
                }

                if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
                    scrollDist += dy;
                }
            }

            private void show() {
                binding.searchLayout.animate().translationY(0)
                        .setInterpolator(new DecelerateInterpolator(2))
                        .start();
            }

            private void hide() {
                int height = binding.searchLayout.getHeight();
                binding.searchLayout.animate().translationY(-height)
                        .setInterpolator(new AccelerateInterpolator(2))
                        .start();
                if (getBaseActivity() != null) {
                    getBaseActivity().hideKeyboard();
                }
            }

        });
    }

    private void setupSearchView() {
        //make the entire area clickable
        binding.searchView.setOnClickListener(v -> binding.searchView.onActionViewExpanded());
        // the view model debounces the text and runs the search
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.onQueryChanged(query);
                if (getBaseActivity() != null) {
                    getBaseActivity().hideKeyboard();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.onQueryChanged(newText);
                return true;
            }
        });
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

    // keeps the first item below the search layout, the spacing between items comes from VerticalItemDecoration
    private static class TopItemDecoration extends RecyclerView.ItemDecoration {

        private final int paddingTop;

        public TopItemDecoration(int paddingTop) {
            this.paddingTop = paddingTop;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = paddingTop;
            }
        }
    }

}
