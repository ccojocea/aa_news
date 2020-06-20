package com.ccojocea.aanews.ui.common;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalItemDecoration extends RecyclerView.ItemDecoration {

    private static final int DEFAULT_VERTICAL_SPACE_HEIGHT = 25;
    private final int verticalSpaceHeight;

    public VerticalItemDecoration() {
        this.verticalSpaceHeight = DEFAULT_VERTICAL_SPACE_HEIGHT;
    }

    public VerticalItemDecoration(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = verticalSpaceHeight;
    }

}