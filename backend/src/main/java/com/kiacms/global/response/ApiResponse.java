package com.kiacms.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;
    private final Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder()
                .success(true)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .timestamp(Instant.now())
                .build();
    }
}
