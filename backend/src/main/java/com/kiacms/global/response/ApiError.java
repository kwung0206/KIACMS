package com.kiacms.global.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiError {

    private final String code;
    private final String message;
    private final List<FieldValidationError> fieldErrors;

    public static ApiError of(String code, String message) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .fieldErrors(List.of())
                .build();
    }

    public static ApiError of(String code, String message, List<FieldValidationError> fieldErrors) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .fieldErrors(fieldErrors == null ? List.of() : fieldErrors)
                .build();
    }
}
