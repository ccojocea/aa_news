package com.ccojocea.aanews.common;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeUtil {

    private static final int FORMAT_LENGTH = 10;

    public static final String SERVER_TIME_FORMAT_1 = "yyyy-MM-dd";
    public static final String SERVER_TIME_FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss";


    @NonNull
    public static CharSequence convertToReadableTime(@Nullable String serverTime) {
        if (serverTime != null) {
            SimpleDateFormat sdf;
            if (serverTime.length() > FORMAT_LENGTH) {
                sdf = new SimpleDateFormat(SERVER_TIME_FORMAT_2, Locale.getDefault());
            } else {
                sdf = new SimpleDateFormat(SERVER_TIME_FORMAT_1, Locale.getDefault());
            }
            try {
                long time = sdf.parse(serverTime).getTime();
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            } catch (ParseException | NullPointerException e) {
                return "";
            }
        } else {
            return "";
        }
    }

}
