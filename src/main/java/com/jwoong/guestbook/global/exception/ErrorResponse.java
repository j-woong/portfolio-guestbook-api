package com.jwoong.guestbook.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

// 모든 에러 응답의 형식. errors는 필드별 검증 메시지가 있을 때만 포함한다
public record ErrorResponse(String message, @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errors) {

    public ErrorResponse(String message) {
        this(message, null);
    }
}
