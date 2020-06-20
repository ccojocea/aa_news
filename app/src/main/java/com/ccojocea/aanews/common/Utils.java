package com.ccojocea.aanews.common;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.ccojocea.aanews.R;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

}
