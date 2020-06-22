package com.ccojocea.aanews.common;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.ccojocea.aanews.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.ConnectException;
import java.net.UnknownHostException;

public class Utils {

    private static long lastClickTime;
    private static final long DELAY = 500;

    public static boolean shouldPreventMisClick() {
        if (SystemClock.elapsedRealtime() - lastClickTime <= DELAY) {
            return true;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        return false;
    }

    public static void shareLink(@NonNull Context context, @NonNull String articleUrl) {
        shouldPreventMisClick();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.putExtra(Intent.EXTRA_TEXT, articleUrl);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_article_title)));
    }

    @NonNull
    public static Gson getAppGson() {
        return new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .setPrettyPrinting()
                .create();
    }

    public static void showSnackBar(View root, String message, int length, int gravity) {
        Snackbar snackbar = Snackbar.make(root, message, length);
        snackbar.setBackgroundTint(ContextCompat.getColor(root.getContext(), R.color.secondaryColor));
        snackbar.show();
        View view = snackbar.getView();
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setGravity(gravity);
    }

    public static String getErrorMessage(Throwable throwable) {
        if (throwable != null && throwable.getMessage() != null) {
            if (throwable instanceof UnknownHostException || throwable instanceof ConnectException) {
                return App.getApp().getString(R.string.no_internet);
            }
        }
        return App.getApp().getString(R.string.error_please_try_again);
    }

}
