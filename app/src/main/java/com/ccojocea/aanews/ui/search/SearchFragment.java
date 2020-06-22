package com.ccojocea.aanews.ui.search;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.databinding.FragmentSearchBinding;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.ui.MainActivity;
import com.ccojocea.aanews.ui.SharedViewModel;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;
import com.ccojocea.aanews.ui.common.VerticalItemDecoration;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SearchFragment extends BaseFragment implements SharedRecyclerViewAdapter.NewsAdapterListener {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private SharedRecyclerViewAdapter adapter;

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

        // disable SearchView underline highlight
        View v = binding.searchView.findViewById(com.google.android.material.R.id.search_plate);
        //noinspection ConstantConditions
        v.setBackgroundColor(getResources().getColor(R.color.primaryDarkColor, getContext().getTheme()));

        adapter = new SharedRecyclerViewAdapter();
        adapter.setAdapterListener(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new VerticalItemDecoration());
        binding.recyclerView.setAdapter(adapter);

        sharedViewModel.getSwipeData().observe(getViewLifecycleOwner(), isSwipeEnabled -> {
            Timber.d("Preference - Swipe Data - Search: %s", isSwipeEnabled);
            adapter.setItemViewSwipeEnabled(!isSwipeEnabled);
        });

        viewModel.getResultsData().observe(getViewLifecycleOwner(), articleEntityList -> {
            if (articleEntityList.size() > 0) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.emptyView.setVisibility(View.INVISIBLE);
                adapter.setItems(articleEntityList);
                adapter.notifyDataSetChanged();
            } else {
                binding.recyclerView.setVisibility(View.INVISIBLE);
                binding.emptyView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                viewModel.resetError();
                if (getContext() != null) {
                    Utils.showSnackBar(((MainActivity)getContext()).getRoot(), errorMessage, Snackbar.LENGTH_LONG, Gravity.CENTER_HORIZONTAL);
                }
            }
        });

        if (getContext() != null) {
            adapter.setupSwipeCallback(binding.recyclerView, getContext());
        }

        setupAnimations();
        setupSearchView();
    }

    private void setupAnimations() {
        ViewTreeObserver viewTreeObserver = binding.searchLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
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
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Timber.d("Recycler onScrollChange: %d", newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Timber.d("Recycler onScrollChange: %d - isVisible %s - scrollDist %s", dy, isVisible, scrollDist);

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
        Observable<String> observableQuery = Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(newText);
                                }
                                return false;
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io());

        viewModel.setupSearchObserver(observableQuery);
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

    @Override
    public void onBookmarkClicked(int position, String url, boolean shouldSave) {
        if (shouldSave) {
            ArticleEntity articleEntity = adapter.getItem(position);
            if (articleEntity != null) {
                viewModel.bookmarkArticle(articleEntity);
            }
        } else {
            viewModel.removeBookmarkedArticle(url);
        }
    }

    private static class TopItemDecoration extends VerticalItemDecoration {

        private final int paddingTop;

        public TopItemDecoration(int paddingTop) {
            this.paddingTop = paddingTop;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top += paddingTop;
            }
        }
    }

}
