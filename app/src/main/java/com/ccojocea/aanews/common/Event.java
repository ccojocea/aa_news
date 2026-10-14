package com.ccojocea.aanews.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Wraps LiveData values that should be handled once (snackbars, navigation), not again after a configuration change
 * and not by more than one observer.
 */
public class Event<T> {

    @NonNull
    private final T content;
    private boolean hasBeenHandled;

    public Event(@NonNull T content) {
        this.content = content;
    }

    /**
     * Returns the content the first time it is called and null afterwards.
     */
    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        }
        hasBeenHandled = true;
        return content;
    }

    @NonNull
    public T peekContent() {
        return content;
    }

}
