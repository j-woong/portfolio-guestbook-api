# 04. 예외 처리와 CORS

| 키워드 | 한 줄 설명 |
|---|---|
| `@RestControllerAdvice` | 모든 컨트롤러에서 난 예외를 한곳에서 받아 응답으로 바꾼다 |
| ErrorCode | 에러별 상태 코드와 메시지를 모은 enum |
| BusinessException | 서비스가 "처리할 수 없는 요청"을 알릴 때 던지는 예외 |
| CORS | 다른 주소의 웹페이지가 이 서버를 호출해도 되는지 브라우저가 확인하는 규칙 |
| Preflight | 실제 요청 전에 브라우저가 보내는 OPTIONS 확인 요청 |

## 공통 개념: 에러 응답은 항상 같은 모양

어떤 에러든 `{"message": "..."}` 형식으로 나간다. 프론트는 `message`만 읽으면 된다. 입력값 검증 실패일 때만 `errors`에 필드별 메시지가 추가된다.

## 예외 처리

- 서비스는 `throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS_EXCEPTION)`처럼 에러 코드만 던진다. 상태 코드와 메시지는 `ErrorCode`에 있고, `GlobalExceptionHandler`가 응답으로 바꾼다.
- 새 에러가 필요하면 `ErrorCode`에 한 줄만 추가한다. EFY의 팀 규칙과 같은 구조다.
- 스프링이 던지는 예외(검증 실패, 잘못된 JSON, 타입 불일치, 없는 주소, 지원하지 않는 메서드)도 같은 형식으로 바꾼다.
- 예상하지 못한 예외는 500으로 응답하고 원인은 로그에만 남긴다. 스택 트레이스나 SQL이 응답에 나가지 않는다.

## CORS

- 포트폴리오 사이트(vercel.app)와 API 서버(cloudtype.app)는 주소가 다르다. 브라우저는 기본적으로 다른 주소로의 요청 결과를 막기 때문에, 서버가 "이 주소는 허용한다"고 알려줘야 한다.
- `CorsConfig`가 `/api/**`에 대해 `app.allowed-origins`에 적힌 주소만 허용한다. 로컬은 `application-local.properties`, 배포는 환경변수 `FRONTEND_URL`에서 읽는다.
- 허용한 것: GET·POST·DELETE 메서드, `Content-Type`·`X-Admin-Key` 헤더.
- CORS는 브라우저가 지키는 규칙이다. curl 같은 도구는 그대로 호출할 수 있으므로, 도배 방지와 관리자 키는 CORS와 별개로 필요하다.
