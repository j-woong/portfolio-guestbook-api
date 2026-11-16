package com.jwoong.guestbook.park;

// 한 방문자의 현재 상태. 다른 방문자들에게 그대로 전달된다
// t: 메시지 종류("s"), id: 방문자 번호, x·z: 위치, f: 바라보는 방향, y: 점프 높이, n: 닉네임
public record PlayerState(String t, String id, double x, double z, double f, double y, String n) {
}
