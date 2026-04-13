package com.kiacms.global.exception;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(ErrorCode.DATA_CONFLICT, message);
    }
}
