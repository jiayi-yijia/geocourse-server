package com.bddk.geocourse.framework.common.api;

import java.io.Serializable;

public record ApiResponse<T>(Integer code, String message, T data) implements Serializable {

    public static final int SUCCESS = 0;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS, "OK", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(SUCCESS, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

}

