package com.ccojocea.aanews.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Helpers for the user's "My News" topics.
 */
public final class Topics {

    public static final int MAX_TOPICS = 10;
    public static final int MAX_TOPIC_LENGTH = 40;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private Topics() {
    }

    /**
     * Trims and cleans a topic typed by the user.
     *
     * @return null if nothing usable is left or the topic is too long
     */
    @Nullable
    public static String normalize(@Nullable String topic) {
        if (topic == null) {
            return null;
        }
        // quotes are added by buildQuery, the user can't nest them
        String normalized = WHITESPACE.matcher(topic.replace("\"", " ")).replaceAll(" ").trim();
        if (normalized.isEmpty() || normalized.length() > MAX_TOPIC_LENGTH) {
            return null;
        }
        return normalized;
    }

    /**
     * @return a new list with the topic appended, unless it is invalid, already present (ignoring case)
     * or the list is full, in which case the result has the same items as the input
     */
    @NonNull
    public static List<String> add(@NonNull List<String> topics, @Nullable String topic) {
        List<String> result = new ArrayList<>(topics);
        String normalized = normalize(topic);
        if (normalized == null || result.size() >= MAX_TOPICS || contains(result, normalized)) {
            return result;
        }
        result.add(normalized);
        return result;
    }

    public static boolean contains(@NonNull Collection<String> topics, @NonNull String topic) {
        String lowerCase = topic.toLowerCase(Locale.ROOT);
        for (String existing : topics) {
            if (existing.toLowerCase(Locale.ROOT).equals(lowerCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds the "q" parameter of the Everything API: any of the topics, phrases are matched exactly.
     * e.g. [Android, machine learning] -> Android OR "machine learning"
     */
    @NonNull
    public static String buildQuery(@NonNull Collection<String> topics) {
        StringBuilder query = new StringBuilder();
        for (String topic : topics) {
            if (query.length() > 0) {
                query.append(" OR ");
            }
            if (topic.contains(" ")) {
                query.append('"').append(topic).append('"');
            } else {
                query.append(topic);
            }
        }
        return query.toString();
    }

}
