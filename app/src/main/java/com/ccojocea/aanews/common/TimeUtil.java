package com.ccojocea.aanews.common;

import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    private static final int FORMAT_LENGTH = 10;

    public static final String SERVER_TIME_FORMAT_1 = "yyyy-MM-dd";
    public static final String SERVER_TIME_FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss";

    @NonNull
    public static CharSequence convertToReadableTime(@Nullable String serverTime) {
        Long time = parseServerTime(serverTime);
        if (time == null) {
            return "";
        }
        long now = System.currentTimeMillis();
        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
    }

    /**
     * Parses the publishedAt value of the API, e.g. "2020-06-12T14:22:06Z" (always UTC).
     * Anything after the seconds (milliseconds, the 'Z') is ignored.
     *
     * @return the time in milliseconds or null if the value can't be parsed
     */
    @Nullable
    public static Long parseServerTime(@Nullable String serverTime) {
        if (serverTime == null) {
            return null;
        }
        // SimpleDateFormat is not thread safe, create a new one each time
        SimpleDateFormat sdf = new SimpleDateFormat(serverTime.length() > FORMAT_LENGTH ? SERVER_TIME_FORMAT_2 : SERVER_TIME_FORMAT_1, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(serverTime);
            return date != null ? date.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }

}
