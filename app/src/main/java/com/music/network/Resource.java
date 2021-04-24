package com.music.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Resource<T> {
    @NonNull
    public final Status status;

    @Nullable
    public T data;

    @Nullable
    public String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    @NonNull
    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    @NonNull
    public static <T> Resource<T> loading(@NonNull String message) {
        return new Resource<>(Status.LOADING, null, message);
    }

    @NonNull
    public static <T> Resource<T> error(@NonNull String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }
}
