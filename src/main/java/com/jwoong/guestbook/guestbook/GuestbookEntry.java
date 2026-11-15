package com.jwoong.guestbook.guestbook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// 방명록 글 한 건
@Entity
@Table(name = "guestbook_entry")
public class GuestbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String nickname;

    @Column(nullable = false, length = 140)
    private String message;

    // 숨은 고양이를 찾은 방문자인지 여부
    @Column(nullable = false)
    private boolean foundCat;

    // 시간대와 무관하게 같은 순간을 가리키도록 UTC 기준 Instant로 저장한다
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // JPA가 조회 결과를 객체로 만들 때 쓰는 기본 생성자
    protected GuestbookEntry() {
    }

    public GuestbookEntry(String nickname, String message, boolean foundCat) {
        this.nickname = nickname;
        this.message = message;
        this.foundCat = foundCat;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFoundCat() {
        return foundCat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
