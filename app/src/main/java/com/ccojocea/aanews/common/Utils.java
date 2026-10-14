package com.ccojocea.aanews.common;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.models.responses.NewsApiError;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

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

    public static void showSnackBar(View root, String message, int length, boolean isError) {
        showSnackBar(root, message, length, isError, null);
    }

    public static void showSnackBar(View root, String message, int length, boolean isError, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(root, message, length);
        if (isError) {
            snackbar.setBackgroundTint(ContextCompat.getColor(root.getContext(), R.color.secondaryColor));
        } else {
            snackbar.setBackgroundTint(ContextCompat.getColor(root.getContext(), R.color.primaryDarkColor));
        }
        if (onClickListener != null) {
            snackbar.setAction(R.string.undo_action, onClickListener);
        } else {
            View view = snackbar.getView();
            TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        snackbar.show();
    }

    @NonNull
    public static String getErrorMessage(@Nullable Throwable throwable) {
        return App.getApp().getString(getErrorMessageRes(throwable));
    }

    @StringRes
    private static int getErrorMessageRes(@Nullable Throwable throwable) {
        if (throwable instanceof UnknownHostException || throwable instanceof ConnectException) {
            return R.string.no_internet;
        }
        if (throwable instanceof SocketTimeoutException) {
            return R.string.error_timeout;
        }
        if (throwable instanceof HttpException) {
            switch (getNewsApiError((HttpException) throwable)) {
                case API_KEY_MISSING:
                    return R.string.error_api_key_missing;
                case API_KEY_INVALID:
                case API_KEY_DISABLED:
                    return R.string.error_api_key_invalid;
                case API_KEY_EXHAUSTED:
                case RATE_LIMITED:
                    return R.string.error_rate_limited;
                case MAXIMUM_RESULTS_REACHED:
                    return R.string.error_maximum_results;
                case UNEXPECTED_ERROR:
                    return R.string.error_server;
                default:
                    break;
            }
        }
        return R.string.error_please_try_again;
    }

    @NonNull
    public static NewsApiError getNewsApiError(@NonNull HttpException exception) {
        NewsApiError error = NewsApiError.UNKNOWN;
        Response<?> response = exception.response();
        if (response != null && response.errorBody() != null) {
            try {
                // retrofit buffers error bodies, reading it doesn't touch the network
                //noinspection ConstantConditions
                error = NewsApiError.fromErrorBody(response.errorBody().string());
            } catch (IOException e) {
                Timber.e(e, "Could not read the error body");
            }
        }
        return error != NewsApiError.UNKNOWN ? error : NewsApiError.fromHttpStatus(exception.code());
    }

}
