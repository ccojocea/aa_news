package com.ccojocea.aanews.ui.common;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.Utils;

public abstract class BaseActivity extends AppCompatActivity {

    private long lastBackPress;
    private static final long EXIT_DELAY = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apps targeting SDK 35+ are always drawn edge-to-edge, enable it everywhere for a consistent look
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        if (shouldConfirmExit()) {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    onExitRequested();
                }
            });
        }
    }

    /**
     * Return true to require a second back press (within {@link #EXIT_DELAY}) before closing the activity.
     */
    protected boolean shouldConfirmExit() {
        return false;
    }

    private void onExitRequested() {
        if (SystemClock.elapsedRealtime() - lastBackPress <= EXIT_DELAY) {
            finish();
        } else {
            lastBackPress = SystemClock.elapsedRealtime();
            if (getRoot() != null) {
                Utils.showSnackBar(getRoot(), getString(R.string.back_to_exit), Toast.LENGTH_SHORT, false);
            } else {
                Toast.makeText(this, R.string.back_to_exit, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public abstract View getRoot();

    /**
     * Keeps the content out of the system bars: the top view (usually the toolbar) is padded below the status bar,
     * the bottom view is padded above the navigation bar and the root is padded horizontally (landscape, cutouts).
     */
    protected void applySystemBarInsets(@NonNull View root, @Nullable View topView, @Nullable View bottomView) {
        final int rootLeft = root.getPaddingLeft();
        final int rootRight = root.getPaddingRight();
        final int topViewTop = topView != null ? topView.getPaddingTop() : 0;
        final int topViewMinHeight = topView != null ? topView.getMinimumHeight() : 0;
        final int bottomViewBottom = bottomView != null ? bottomView.getPaddingBottom() : 0;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(rootLeft + insets.left, v.getPaddingTop(), rootRight + insets.right, v.getPaddingBottom());
            if (topView != null) {
                // grow by the same amount, otherwise the padding is taken from the toolbar content
                topView.setMinimumHeight(topViewMinHeight + insets.top);
                topView.setPadding(topView.getPaddingLeft(), topViewTop + insets.top, topView.getPaddingRight(), topView.getPaddingBottom());
            }
            if (bottomView != null) {
                bottomView.setPadding(bottomView.getPaddingLeft(), bottomView.getPaddingTop(), bottomView.getPaddingRight(), bottomViewBottom + insets.bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}
