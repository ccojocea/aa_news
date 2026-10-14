package com.ccojocea.aanews.ui.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Asks for more items when the list is scrolled close to its end.
 */
public class LoadMoreScrollListener extends RecyclerView.OnScrollListener {

    // start loading while this many items are still left to scroll through
    private static final int THRESHOLD = 5;

    @NonNull
    private final Runnable onLoadMore;

    public LoadMoreScrollListener(@NonNull Runnable onLoadMore) {
        this.onLoadMore = onLoadMore;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int lastVisible = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            if (lastVisible >= layoutManager.getItemCount() - THRESHOLD) {
                onLoadMore.run();
            }
        }
    }

}
