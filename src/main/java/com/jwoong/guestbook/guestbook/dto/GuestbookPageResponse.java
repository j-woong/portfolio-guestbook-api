package com.jwoong.guestbook.guestbook.dto;

import java.util.List;

// 목록 응답. hasNext가 true면 다음 페이지가 있다
public record GuestbookPageResponse(List<GuestbookEntryResponse> entries, int page, int size, boolean hasNext) {
}
