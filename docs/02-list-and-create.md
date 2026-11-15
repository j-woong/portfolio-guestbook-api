# 02. 목록 조회와 글 작성

| 키워드 | 한 줄 설명 |
|---|---|
| Slice | "다음 페이지가 있는지"만 알려주는 페이징 결과. 전체 개수를 세지 않는다 |
| `@Valid` | 요청 DTO의 `@NotBlank`, `@Size` 조건을 컨트롤러에서 검사한다 |
| `@Transactional(readOnly = true)` | 조회 전용 트랜잭션. 쓰기 메서드에만 `@Transactional`을 따로 붙인다 |
| Instant | 시간대가 없는 UTC 기준 시각. JSON에서는 `2026-11-12T08:16:36Z` 형태 |

## 공통 개념: 엔티티를 그대로 응답하지 않는다

조회와 작성 모두 `GuestbookEntry`(엔티티)를 `GuestbookEntryResponse`(record)로 바꿔서 응답한다. 변환은 `GuestbookEntryResponse.from(entry)` 한 곳에서 한다. 나중에 엔티티에 필드가 추가돼도 응답 모양은 내가 정한 그대로 유지된다.

## 목록 조회 `GET /api/guestbook`

- `findAllByOrderByIdDesc(Pageable)`: 메서드 이름으로 "id 내림차순 전체 조회" 쿼리를 만든다. id가 클수록 최신 글이다.
- 반환 타입이 `Page`가 아니라 `Slice`다. `Page`는 전체 개수를 세는 count 쿼리를 한 번 더 보내는데, 방명록은 "더 보기"만 있으면 돼서 필요 없다. `Slice`는 요청한 개수보다 1개 더 읽어서 다음 페이지 여부만 확인한다.
- `page`가 음수이거나 `size`가 너무 크면 에러를 내지 않고 0과 30으로 맞춘다. 누군가 `size=100000`을 보내도 30개만 읽는다.

## 글 작성 `POST /api/guestbook`

- `GuestbookCreateRequest`의 조건: 닉네임 1~12자, 메시지 1~140자. 공백만 있는 값은 `@NotBlank`가 막는다.
- `foundCat`은 `Boolean`(대문자)이다. 프론트가 값을 안 보내면 null이 들어오고, 서비스에서 `Boolean.TRUE.equals(...)`로 false 처리한다.
- 저장 전에 `strip()`으로 앞뒤 공백을 지운다.
- 엔티티의 컬럼 길이(`length = 12`, `length = 140`)를 DTO 검증과 같게 맞췄다. 검증을 통과한 값은 항상 DB에 들어간다.
- 작성 시각은 `Instant.now()`로 서버가 정한다. 프론트가 보낸 시각은 받지 않는다.
