package com.jwoong.guestbook.park;

import java.util.List;

// 서버가 보내는 메시지 모양
public final class ParkMessages {

    private ParkMessages() {
    }

    // 접속 직후 한 번: 내 번호와 지금 공원에 있는 사람들
    public record Hello(String t, String id, List<PlayerState> players) {
        public Hello(String id, List<PlayerState> players) {
            this("hello", id, players);
        }
    }

    // 누군가 공원을 나갔을 때
    public record Bye(String t, String id) {
        public Bye(String id) {
            this("bye", id);
        }
    }
}
