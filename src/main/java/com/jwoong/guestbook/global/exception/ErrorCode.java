package com.jwoong.guestbook.global.exception;

import org.springframework.http.HttpStatus;

// 에러마다 상태 코드와 메시지를 한곳에 모은다. 새 에러는 여기에 한 줄 추가한다
public enum ErrorCode {
    INVALID_INPUT_EXCEPTION(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    INVALID_JSON_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다."),
    INVALID_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다."),
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    ENTRY_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "방명록 글을 찾을 수 없습니다."),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED_EXCEPTION(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    TOO_MANY_REQUESTS_EXCEPTION(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 남겨 주세요."),
    INTERNAL_SERVER_ERROR_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
