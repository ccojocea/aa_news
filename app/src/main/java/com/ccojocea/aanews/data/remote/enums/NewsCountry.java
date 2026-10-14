package com.ccojocea.aanews.data.remote.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * Used by TopHeadlines API
 */
public enum NewsCountry {
    AE,
    AR,
    AT,
    AU,
    BE,
    BG,
    BR,
    CA,
    CH,
    CN,
    CO,
    CU,
    CZ,
    DE,
    EG,
    FR,
    GB,
    GR,
    HK,
    HU,
    ID,
    IE,
    IL,
    IN,
    IT,
    JP,
    KR,
    LT,
    LV,
    MA,
    MX,
    MY,
    NG,
    NL,
    NO,
    NZ,
    PH,
    PL,
    PT,
    RO,
    RS,
    RU,
    SA,
    SE,
    SG,
    SI,
    SK,
    TH,
    TR,
    TW,
    UA,
    US,
    VE,
    ZA;

    public static final NewsCountry DEFAULT = US;

    /**
     * Lowercase code expected by the API.
     */
    @NonNull
    public String getValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Country name in the user's language, e.g. "Romania" for RO.
     */
    @NonNull
    public String getDisplayName() {
        return new Locale("", name()).getDisplayCountry();
    }

    @Nullable
    public static NewsCountry fromValue(@Nullable String value) {
        if (value != null) {
            for (NewsCountry country : values()) {
                if (country.name().equalsIgnoreCase(value)) {
                    return country;
                }
            }
        }
        return null;
    }

    /**
     * The country of the device if the API supports it, otherwise {@link #DEFAULT}.
     */
    @NonNull
    public static NewsCountry fromLocale(@NonNull Locale locale) {
        NewsCountry country = fromValue(locale.getCountry());
        return country != null ? country : DEFAULT;
    }
}
