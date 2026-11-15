package com.jwoong.guestbook.guestbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestbookCreateRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(max = 12, message = "닉네임은 12자 이하로 입력해 주세요.")
        String nickname,

        @NotBlank(message = "메시지를 입력해 주세요.")
        @Size(max = 140, message = "메시지는 140자 이하로 입력해 주세요.")
        String message,

        // 보내지 않으면 null이고, 서비스에서 false로 처리한다
        Boolean foundCat
) {
}
