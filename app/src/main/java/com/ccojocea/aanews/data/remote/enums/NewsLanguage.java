package com.ccojocea.aanews.data.remote.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * Used by the Everything API
 */
public enum NewsLanguage {
    ALL(null),
    AR("ar"),
    DE("de"),
    EN("en"),
    ES("es"),
    FR("fr"),
    HE("he"),
    IT("it"),
    NL("nl"),
    NO("no"),
    PT("pt"),
    RU("ru"),
    SV("sv"),
    UD("ud"),
    ZH("zh");

    @Nullable
    private final String value;

    NewsLanguage(@Nullable String value) {
        this.value = value;
    }

    /**
     * Code expected by the API, null for all languages.
     */
    @Nullable
    public String getValue() {
        return value;
    }

    /**
     * Language name in the user's language, null for {@link #ALL}.
     */
    @Nullable
    public String getDisplayName() {
        if (value == null) {
            return null;
        }
        // the API uses "ud" for Urdu, the ISO 639-1 code is "ur"
        String isoCode = this == UD ? "ur" : value;
        return new Locale(isoCode).getDisplayLanguage();
    }

    @NonNull
    public static NewsLanguage fromValue(@Nullable String value) {
        if (value != null) {
            for (NewsLanguage language : values()) {
                if (value.equals(language.value)) {
                    return language;
                }
            }
        }
        return ALL;
    }
}
