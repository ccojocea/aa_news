package com.ccojocea.aanews.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.models.entity.ArticleEntity;

import java.lang.reflect.Type;

import timber.log.Timber;

public class SwipeCallback extends ItemTouchHelper.Callback {

    private static float SWIPE_THRESHOLD = 0.7f;

    Context mContext;

    private Paint mClearPaint;
    private ColorDrawable mBackground;

    private int bookmarkBackgroundColor;
    private int shareBackgroundColor;
    private Drawable saveDrawable;
    private Drawable removeDrawable;
    private Drawable shareDrawable;
    private int bookmarkIntrinsicWidth;
    private int bookmarkIntrinsicHeight;
    private int shareIntrinsicWidth;
    private int shareIntrinsicHeight;

    public SwipeCallback(Context context) {
        mContext = context;
        mBackground = new ColorDrawable();
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            bookmarkBackgroundColor = a.data;
            shareBackgroundColor = a.data;
        } else {
            bookmarkBackgroundColor = context.getColor(R.color.primaryDarkColor);
            shareBackgroundColor = context.getColor(R.color.primaryDarkColor);
        }
        shareDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_share_text);
        removeDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_remove_text);
        saveDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_save_text);
        bookmarkIntrinsicWidth = saveDrawable.getIntrinsicWidth() * 2;
        bookmarkIntrinsicHeight = saveDrawable.getIntrinsicHeight() * 2;
        shareIntrinsicWidth = shareDrawable.getIntrinsicWidth() * 2;
        shareIntrinsicHeight = shareDrawable.getIntrinsicHeight() * 2;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        //TODO Instead of restoring the initial view on cancel, keep the button visible and clickable.
        if (isCancelled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        if (dX < 0) {
            //swiped left - share
            mBackground.setColor(shareBackgroundColor);
            mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            mBackground.draw(c);

            int shareIconTop = itemView.getTop() + (itemHeight - shareIntrinsicHeight) / 2;
//            int shareIconMargin = (itemHeight - shareIntrinsicHeight) / 2;
            int shareIconMargin = 0;
            int shareIconLeft = itemView.getRight() - shareIconMargin - shareIntrinsicWidth;
            int shareIconRight = itemView.getRight() - shareIconMargin;
            int shareIconBottom = shareIconTop + shareIntrinsicHeight;

            if (Math.abs((int) dX) > (shareIconMargin)) {
                shareDrawable.setBounds(shareIconLeft, shareIconTop, shareIconRight, shareIconBottom);
                shareDrawable.draw(c);
            }
            Timber.d("Save/Remove dX = %s", dX);
        } else if (dX > 0) {
            //swiped right - bookmark / remove bookmark
            mBackground.setColor(bookmarkBackgroundColor);
            mBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
            mBackground.draw(c);

            int bookmarkIconTop = itemView.getTop() + (itemHeight - bookmarkIntrinsicHeight) / 2;
//            int bookmarkIconMargin = (itemHeight - bookmarkIntrinsicHeight) / 2;
            int bookmarkIconMargin = 0;
            int bookmarkIconLeft = itemView.getLeft() + bookmarkIconMargin;
            int bookmarkIconRight = itemView.getLeft() + bookmarkIconMargin + bookmarkIntrinsicWidth;
            int bookmarkIconBottom = bookmarkIconTop + bookmarkIntrinsicHeight;

            if (dX > bookmarkIconMargin) {
                if (recyclerView.getAdapter() instanceof SharedRecyclerViewAdapter) {
                    ArticleEntity articleEntity = ((SharedRecyclerViewAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
                    if (articleEntity != null) {
                        //TODO Find a way for the text not to change immediately (notifyItemChanged is being called)
                        if (articleEntity.isSaved()) {
                            Timber.d("Save/Remove dX = %s and showing: %s", dX, "Remove");
                            removeDrawable.setBounds(bookmarkIconLeft, bookmarkIconTop, bookmarkIconRight, bookmarkIconBottom);
                            removeDrawable.draw(c);
                        } else {
                            Timber.d("Save/Remove dX = %s and showing: %s", dX, "Save");
                            saveDrawable.setBounds(bookmarkIconLeft, bookmarkIconTop, bookmarkIconRight, bookmarkIconBottom);
                            saveDrawable.draw(c);
                        }
                    } else {
                        Timber.e("Save/Remove Article null");
                    }
                }
            }
        } else {
            Timber.d("Save/Remove dX = 0");
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return SWIPE_THRESHOLD;
    }
}
