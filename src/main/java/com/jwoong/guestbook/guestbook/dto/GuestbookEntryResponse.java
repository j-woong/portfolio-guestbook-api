package com.jwoong.guestbook.guestbook.dto;

import com.jwoong.guestbook.guestbook.GuestbookEntry;

import java.time.Instant;

public record GuestbookEntryResponse(Long id, String nickname, String message, boolean foundCat, Instant createdAt) {

    public static GuestbookEntryResponse from(GuestbookEntry entry) {
        return new GuestbookEntryResponse(entry.getId(), entry.getNickname(), entry.getMessage(),
                entry.isFoundCat(), entry.getCreatedAt());
    }
}
