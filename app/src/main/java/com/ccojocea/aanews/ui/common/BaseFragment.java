package com.ccojocea.aanews.ui.common;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccojocea.aanews.common.Event;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.ui.SharedViewModel;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseFragment extends Fragment {

    public abstract String getFragmentTitle(Context context);

    /**
     * This method returns BaseActivity.
     * Since the app is using activities that extend BaseActivity there should not be a problem using this method.
     * BaseActivity might be null if the activity finished.
     */
    @Nullable
    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @NonNull
    protected SharedViewModel getSharedViewModel() {
        return new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    /**
     * Common setup for the article lists: layout, item spacing, bookmark clicks and swipe actions.
     * Call from onViewCreated.
     */
    protected void setupArticleList(@NonNull RecyclerView recyclerView, @NonNull SharedRecyclerViewAdapter adapter) {
        SharedViewModel sharedViewModel = getSharedViewModel();

        adapter.setAdapterListener(sharedViewModel::onBookmarkClicked);
        if (recyclerView.getItemAnimator() != null) {
            recyclerView.getItemAnimator().setChangeDuration(0);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new VerticalItemDecoration());
        recyclerView.setAdapter(adapter);

        // add the SwipeCallback to the recyclerView.adapter
        // if viewPager swipe is enabled, disable item swipe on the recyclerView
        adapter.setupSwipeCallback(recyclerView, requireContext());
        sharedViewModel.getSwipeData().observe(getViewLifecycleOwner(), isViewPagerSwipeEnabled ->
                adapter.setItemViewSwipeEnabled(!Boolean.TRUE.equals(isViewPagerSwipeEnabled)));
    }

    protected void showError(@Nullable Event<String> errorEvent) {
        String message = errorEvent != null ? errorEvent.getContentIfNotHandled() : null;
        BaseActivity activity = getBaseActivity();
        if (message != null && activity != null && activity.getRoot() != null) {
            Utils.showSnackBar(activity.getRoot(), message, Snackbar.LENGTH_LONG, true);
        }
    }

}
