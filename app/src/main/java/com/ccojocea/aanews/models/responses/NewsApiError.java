package com.ccojocea.aanews.models.responses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Error codes returned by https://newsapi.org in the body of a failed request.
 * See https://newsapi.org/docs/errors
 */
public enum NewsApiError {
    API_KEY_DISABLED("apiKeyDisabled"),
    API_KEY_EXHAUSTED("apiKeyExhausted"),
    API_KEY_INVALID("apiKeyInvalid"),
    API_KEY_MISSING("apiKeyMissing"),
    PARAMETER_INVALID("parameterInvalid"),
    PARAMETERS_MISSING("parametersMissing"),
    RATE_LIMITED("rateLimited"),
    SOURCES_TOO_MANY("sourcesTooMany"),
    SOURCE_DOES_NOT_EXIST("sourceDoesNotExist"),
    MAXIMUM_RESULTS_REACHED("maximumResultsReached"),
    UNEXPECTED_ERROR("unexpectedError"),
    UNKNOWN(null);

    private static final Gson GSON = new Gson();

    @Nullable
    private final String code;

    NewsApiError(@Nullable String code) {
        this.code = code;
    }

    @NonNull
    public static NewsApiError fromCode(@Nullable String code) {
        if (code != null) {
            for (NewsApiError error : values()) {
                if (code.equals(error.code)) {
                    return error;
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * Reads the error from a response body like {"status":"error","code":"apiKeyInvalid","message":"..."}
     */
    @NonNull
    public static NewsApiError fromErrorBody(@Nullable String body) {
        if (body == null || body.isEmpty()) {
            return UNKNOWN;
        }
        try {
            BaseResponse response = GSON.fromJson(body, BaseResponse.class);
            return response != null ? fromCode(response.code) : UNKNOWN;
        } catch (JsonParseException e) {
            return UNKNOWN;
        }
    }

    /**
     * Fallback for responses without a readable body.
     */
    @NonNull
    public static NewsApiError fromHttpStatus(int httpStatus) {
        switch (httpStatus) {
            case 400:
                return PARAMETER_INVALID;
            case 401:
                return API_KEY_INVALID;
            case 429:
                return RATE_LIMITED;
            case 500:
                return UNEXPECTED_ERROR;
            default:
                return UNKNOWN;
        }
    }

}
