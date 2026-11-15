# 01. 구조와 요청 흐름

| 키워드 | 한 줄 설명 |
|---|---|
| Controller | HTTP 요청을 받아 값을 꺼내고, 서비스에 넘기고, 응답 상태 코드를 정한다 |
| Service | 실제 규칙(도배 방지, 권한 확인, 저장)을 처리한다 |
| Repository | DB에 조회·저장 쿼리를 보낸다 |
| Entity / DTO | Entity는 DB 테이블 한 줄, DTO는 요청·응답 JSON의 모양 |
| record | 값만 담는 클래스를 짧게 쓰는 문법. DTO에 사용 |

## 폴더 구조

```
com.jwoong.guestbook
├── GuestbookApplication      서버 시작점
├── HealthController          /api/health
├── guestbook
│   ├── GuestbookController   요청 받기
│   ├── GuestbookService      규칙 처리
│   ├── GuestbookRepository   DB 접근
│   ├── GuestbookEntry        엔티티 (guestbook_entry 테이블)
│   ├── WriteRateLimiter      30초 작성 제한
│   └── dto                   요청·응답 record
└── global
    ├── config/CorsConfig     다른 주소에서의 호출 허용
    └── exception             에러 코드와 공통 에러 응답
```

EFY 프로젝트와 같은 방식으로, 기능(guestbook)과 공통(global)을 나눴다.

## 글 작성 요청이 지나가는 길

1. 브라우저가 `POST /api/guestbook`으로 JSON을 보낸다.
2. `GuestbookController.create`가 JSON을 `GuestbookCreateRequest`로 바꾸고 `@Valid`로 검증한다. 실패하면 여기서 400으로 끝난다.
3. 컨트롤러가 방문자 IP를 구해 `GuestbookService.create`에 넘긴다.
4. 서비스가 `WriteRateLimiter`에 "이 IP가 지금 써도 되는지" 묻는다. 안 되면 429.
5. 서비스가 `GuestbookEntry`를 만들어 `GuestbookRepository.save`로 저장한다.
6. 저장된 엔티티를 `GuestbookEntryResponse`로 바꿔 201과 함께 돌려준다.

## Lombok을 쓰지 않은 이유

파일 수가 적어서 생성자와 getter를 직접 썼다. DTO는 `record`라서 따로 쓸 코드가 없다. 의존성이 하나 줄고, 코드에 숨은 부분이 없어 읽기 쉽다.
