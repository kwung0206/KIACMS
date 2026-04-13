package com.kiacms.global.response;

public record FieldValidationError(
        String field,
        String message,
        Object rejectedValue
) {
}
