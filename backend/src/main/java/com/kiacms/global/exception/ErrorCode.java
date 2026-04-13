package com.kiacms.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "The request is invalid."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "This HTTP method is not allowed."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password."),
    ACCOUNT_PENDING_APPROVAL(HttpStatus.FORBIDDEN, "ACCOUNT_PENDING_APPROVAL", "Your account is pending approval."),
    ACCOUNT_REJECTED(HttpStatus.FORBIDDEN, "ACCOUNT_REJECTED", "Your account has been rejected."),
    ACCOUNT_WITHDRAWN(HttpStatus.FORBIDDEN, "ACCOUNT_WITHDRAWN", "This account is no longer active."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "The requested resource was not found."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "DATA_CONFLICT", "The request conflicts with existing data."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission to access this resource."),
    AI_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_CONFIGURATION_ERROR", "AI service is not configured."),
    AI_PROVIDER_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE", "AI provider request failed."),
    AI_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "AI_RESPONSE_INVALID", "AI provider returned an invalid response."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected server error occurred.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
