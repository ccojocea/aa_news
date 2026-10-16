package com.ccojocea.aanews.ui.localnews;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.data.remote.enums.NewsCategory;
import com.ccojocea.aanews.databinding.FragmentHeadlinesBinding;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.ui.common.LoadMoreScrollListener;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;
import com.google.android.material.chip.Chip;

public class HeadlinesFragment extends BaseFragment {

    private static final String ABOUT_COUNTRY_TAG = "about_country";

    private FragmentHeadlinesBinding binding;
    private HeadlinesViewModel viewModel;
    private SharedRecyclerViewAdapter adapter;
    private Chip aboutCountryChip;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HeadlinesViewModel.class);
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
        setupArticleList(binding.recyclerView, adapter);
        binding.recyclerView.addOnScrollListener(new LoadMoreScrollListener(viewModel::loadMore));

        binding.refreshLayout.setOnRefreshListener(viewModel::reload);
        // the list is wrapped in a layout, tell the refresh layout when the list itself can still scroll up
        binding.refreshLayout.setOnChildScrollUpCallback((parent, child) -> binding.recyclerView.canScrollVertically(-1));

        setupCategories();
        setupLiveData();
    }

    private void setupCategories() {
        // first chip: news about the country, the others: top headlines by category
        aboutCountryChip = createChip(ABOUT_COUNTRY_TAG);
        aboutCountryChip.setText(getString(R.string.headlines_about_country, viewModel.getCountry().getDisplayName()));
        binding.categoryChips.addView(aboutCountryChip);
        if (viewModel.isAboutCountry()) {
            aboutCountryChip.setChecked(true);
        }

        for (NewsCategory category : NewsCategory.values()) {
            Chip chip = createChip(category);
            chip.setText(getCategoryName(category));
            binding.categoryChips.addView(chip);
            if (!viewModel.isAboutCountry() && category == viewModel.getCategory()) {
                chip.setChecked(true);
            }
        }
        binding.categoryChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            Chip checked = group.findViewById(checkedIds.get(0));
            if (checked == null) {
                return;
            }
            binding.recyclerView.scrollToPosition(0);
            if (checked.getTag() instanceof NewsCategory) {
                viewModel.setCategory((NewsCategory) checked.getTag());
            } else if (ABOUT_COUNTRY_TAG.equals(checked.getTag())) {
                viewModel.showAboutCountry();
            }
        });
    }

    @NonNull
    private Chip createChip(@NonNull Object tag) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_filter_chip, binding.categoryChips, false);
        // chips are created again with new ids together with the view, their state must not be restored
        chip.setId(View.generateViewId());
        chip.setSaveEnabled(false);
        chip.setTag(tag);
        return chip;
    }

    @StringRes
    private static int getCategoryName(@NonNull NewsCategory category) {
        switch (category) {
            case BUSINESS:
                return R.string.category_business;
            case ENTERTAINMENT:
                return R.string.category_entertainment;
            case GENERAL:
                return R.string.category_general;
            case HEALTH:
                return R.string.category_health;
            case SCIENCE:
                return R.string.category_science;
            case SPORTS:
                return R.string.category_sports;
            case TECHNOLOGY:
                return R.string.category_technology;
            case ALL:
            default:
                return R.string.category_all;
        }
    }

    private void setupLiveData() {
        viewModel.getArticlesLiveData().observe(getViewLifecycleOwner(), articleEntities -> {
            adapter.submitList(articleEntities);
            boolean isEmpty = articleEntities.isEmpty();
            binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (isEmpty) {
                String country = viewModel.getCountry().getDisplayName();
                binding.emptyText.setText(viewModel.isAboutCountry()
                        ? getString(R.string.headlines_about_country_empty, country)
                        : getString(R.string.headlines_empty, country));
            }
        });

        viewModel.getCountryLiveData().observe(getViewLifecycleOwner(), country ->
                aboutCountryChip.setText(getString(R.string.headlines_about_country, country.getDisplayName())));

        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> binding.refreshLayout.setRefreshing(isLoading));

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), this::showError);
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

}
