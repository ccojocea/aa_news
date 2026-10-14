package com.ccojocea.aanews.models.responses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaseResponse {

    public static final String STATUS_OK = "ok";
    public static final String STATUS_ERROR = "error";

    @NonNull
    public final String status;

    // only set when status is "error", see NewsApiError for the possible values
    @Nullable
    public final String code;

    @Nullable
    public final String message;

    BaseResponse(@NonNull String status, @Nullable String code, @Nullable String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
