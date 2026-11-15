package com.jwoong.guestbook.global.exception;

// 서비스에서 "이 요청은 처리할 수 없다"를 알릴 때 던지는 예외
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
