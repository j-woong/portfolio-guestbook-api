# portfolio-guestbook-api

포트폴리오 사이트([지웅 숲공원](https://portfolio-chi-gold-99.vercel.app))의 방명록을 저장하는 Spring Boot API입니다.
로그인 없이 닉네임과 한 줄 메시지를 남길 수 있고, 도배를 막기 위해 같은 방문자는 30초에 한 번만 쓸 수 있습니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| 언어 | Java 25 |
| 프레임워크 | Spring Boot 4.1.2, Spring MVC |
| 데이터 접근 | Spring Data JPA |
| 데이터베이스 | H2(로컬), MariaDB(배포) |
| 배포 | Docker, CloudType |

## API

| 메서드 | 주소 | 설명 | 응답 |
|---|---|---|---|
| GET | `/api/guestbook?page=0&size=20` | 최신 글부터 목록 조회 (size 최대 30) | 200 |
| POST | `/api/guestbook` | 글 작성 `{nickname, message, foundCat}` | 201, 검증 실패 400, 연속 작성 429 |
| DELETE | `/api/guestbook/{id}` | 글 삭제. `X-Admin-Key` 헤더 필요 | 204, 키 불일치 403, 없는 글 404 |
| GET | `/api/health` | 서버 상태 확인 | 200 |

에러 응답은 모두 `{"message": "...", "errors": {필드: 메시지}}` 형식이며, `errors`는 입력값 검증 실패일 때만 포함됩니다.

## 로컬 실행

```bash
./gradlew bootRun     # http://localhost:8080
./gradlew test
```

H2 콘솔은 <http://localhost:8080/h2-console> (JDBC URL: `jdbc:h2:mem:guestbookDB`)에서 볼 수 있습니다.

```bash
curl -X POST localhost:8080/api/guestbook -H 'Content-Type: application/json' \
  -d '{"nickname":"손님","message":"잘 보고 갑니다"}'
curl localhost:8080/api/guestbook
```

## 배포 환경변수

| 이름 | 내용 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` (Docker 이미지 기본값) |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MariaDB 주소 |
| `DB_USERNAME` / `DB_PASSWORD` | MariaDB 계정 |
| `FRONTEND_URL` | 포트폴리오 사이트 주소. CORS 허용 대상 |
| `ADMIN_KEY` | 글 삭제용 관리자 키. 비워 두면 삭제 API는 항상 403 |

## 코드 해설

- [01. 구조와 요청 흐름](docs/01-structure-and-flow.md)
- [02. 목록 조회와 글 작성](docs/02-list-and-create.md)
- [03. 도배 방지와 관리자 삭제](docs/03-rate-limit-and-admin.md)
- [04. 예외 처리와 CORS](docs/04-errors-and-cors.md)
