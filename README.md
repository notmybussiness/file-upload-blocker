# 파일 확장자 차단 과제 MVP

과제 목적에 맞춘 최소 구현(MVP)입니다.
과한 설계는 의도적으로 제외하고, 요구사항 충족과 동작 일관성에만 집중했습니다.

## 과제 요구사항 대응
- 고정 확장자 7개(`bat`, `cmd`, `com`, `cpl`, `exe`, `scr`, `js`) 체크/해제 + DB 저장 + 새로고침 유지
- 커스텀 확장자 추가/삭제 + DB 반영
- 커스텀 입력 길이 20자 제한
- 커스텀 최대 200개 제한
- 커스텀 중복 방지(대소문자 무시)

## 기술 스택
- Java 21
- Spring Boot 4
- Spring Data JPA
- Flyway
- H2 (local 실행)
- PostgreSQL (핵심 통합테스트/배포 대상)
- Vanilla JS (단일 화면)

## API
- `GET /api/v1/extensions/policy`
- `PATCH /api/v1/extensions/fixed/{name}`
  - body: `{ "checked": true | false }`
- `POST /api/v1/extensions/custom`
  - body: `{ "name": "sh" }`
- `DELETE /api/v1/extensions/custom/{id}`

### 에러 응답 형식
```json
{
  "code": "VALIDATION_ERROR",
  "message": "custom extension length must be between 1 and 20"
}
```

## 로컬 실행
```bash
./gradlew bootRun
```

접속:
- 화면: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

## 테스트 실행
```bash
./gradlew test
```

포함 테스트:
- 서비스 단위 테스트
- 컨트롤러 슬라이스 테스트
- PostgreSQL(Testcontainers) 핵심 통합테스트

## 배포 환경(PostgreSQL)
다음 환경변수 사용:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

예시:
```bash
DB_URL=jdbc:postgresql://localhost:5432/extension_blocker \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
./gradlew bootRun --args='--spring.profiles.active=postgres'
```

## 과제 성격상 의도적으로 제외한 범위
- 로그인/권한/사용자별 정책
- 실제 파일 업로드 차단 로직
- 관리자 전용 화면 분리
- 감사 로그/모니터링 고도화

자세한 메모는 [docs/assignment-notes.md](docs/assignment-notes.md) 참고.
