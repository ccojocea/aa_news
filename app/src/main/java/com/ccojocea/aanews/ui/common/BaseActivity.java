package com.ccojocea.aanews.ui.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.Utils;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public abstract class BaseActivity extends AppCompatActivity {

    private long lastBackPress;
    private static final long EXIT_DELAY = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNavBarColor();
    }

    @Override
    public void onBackPressed() {
        if (SystemClock.elapsedRealtime() - lastBackPress <= EXIT_DELAY) {
            finish();
        } else {
            lastBackPress = SystemClock.elapsedRealtime();
            if (getRoot() != null) {
                Utils.showSnackBar(getRoot(), getString(R.string.back_to_exit), Toast.LENGTH_SHORT, Gravity.CENTER_HORIZONTAL);
            } else {
                Toast.makeText(this, R.string.back_to_exit, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public abstract View getRoot();

    public void onBackPressedOverride() {
        super.onBackPressed();
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setNavBarColor() {
        Window window = getWindow();
        if (window != null) {
            switch (AppCompatDelegate.getDefaultNightMode()) {
                case MODE_NIGHT_YES:
                    window.setNavigationBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
                    break;
                case MODE_NIGHT_NO:
                    break;
                case MODE_NIGHT_FOLLOW_SYSTEM:
                    int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (currentNightMode) {
                        case Configuration.UI_MODE_NIGHT_NO:
                        case Configuration.UI_MODE_NIGHT_UNDEFINED: //TODO Should be tested for older Android
                            break;
                        case Configuration.UI_MODE_NIGHT_YES:
                            window.setNavigationBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
                            break;
                    }
                    break;
            }
        }
    }

}
