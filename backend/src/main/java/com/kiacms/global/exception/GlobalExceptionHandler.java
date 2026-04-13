package com.kiacms.global.exception;

import com.kiacms.global.response.ApiError;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.response.FieldValidationError;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(ApiError.of(errorCode.getCode(), exception.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return buildValidationErrorResponse(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return buildValidationErrorResponse(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        List<FieldValidationError> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new FieldValidationError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()
                ))
                .toList();

        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.fail(ApiError.of(
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        ErrorCode.VALIDATION_ERROR.getMessage(),
                        fieldErrors
                )));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ApiResponse.fail(ApiError.of(
                        ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                        ErrorCode.METHOD_NOT_ALLOWED.getMessage()
                )));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(ApiResponse.fail(ApiError.of(
                        ErrorCode.INVALID_REQUEST.getCode(),
                        "Request body is missing or malformed."
                )));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(ErrorCode.DATA_CONFLICT.getStatus())
                .body(ApiResponse.fail(ApiError.of(
                        ErrorCode.DATA_CONFLICT.getCode(),
                        "The operation violates a database constraint."
                )));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ApiError.of(
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                )));
    }

    private ResponseEntity<ApiResponse<Void>> buildValidationErrorResponse(List<FieldError> fieldErrors) {
        List<FieldValidationError> validationErrors = fieldErrors.stream()
                .map(this::toFieldValidationError)
                .collect(Collectors.toList());

        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.fail(ApiError.of(
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        ErrorCode.VALIDATION_ERROR.getMessage(),
                        validationErrors
                )));
    }

    private FieldValidationError toFieldValidationError(FieldError fieldError) {
        return new FieldValidationError(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue()
        );
    }
}
