# 파일 확장자 차단 과제 MVP

과제 목적에 맞춘 최소 구현(MVP)입니다.
정책 관리 + 업로드/다운로드 검증까지 구현했으며, 과한 확장 설계는 의도적으로 제외했습니다.

## 구현 기능
- 고정 확장자 7개(`bat`, `cmd`, `com`, `cpl`, `exe`, `scr`, `js`) 체크/해제 + DB 저장 + 새로고침 유지
- 커스텀 확장자 추가/삭제 + DB 반영
- 커스텀 입력 길이 20자 제한
- 커스텀 최대 200개 제한
- 커스텀 중복 방지(대소문자 무시)
- 파일 업로드 API(`multipart/form-data`) + 차단 정책 검증
- 파일 목록 조회 + 다운로드 API

## 저장소 전략 (MVP 결정)
- 파일 본문: Object Storage (`S3` 기본)
- 파일 메타데이터: `PostgreSQL`

로컬 개발 편의를 위해 `local` 프로파일은 디스크 저장소를 사용하고,
배포(`postgres` 프로파일)에서는 S3 저장소를 사용하도록 분리했습니다.

## 기술 스택
- Java 21
- Spring Boot 4
- Spring Data JPA
- Flyway
- H2 (local 실행)
- PostgreSQL (핵심 통합테스트/배포 대상)
- S3 SDK (배포 저장소)
- Vanilla JS (단일 화면)

## API
### 정책 API
- `GET /api/v1/extensions/policy`
- `PATCH /api/v1/extensions/fixed/{name}`
  - body: `{ "checked": true | false }`
- `POST /api/v1/extensions/custom`
  - body: `{ "name": "sh" }`
- `DELETE /api/v1/extensions/custom/{id}`

### 파일 API
- `POST /api/v1/files`
  - Content-Type: `multipart/form-data`
  - field: `file`
- `GET /api/v1/files`
- `GET /api/v1/files/{id}/download`

### 에러 응답 형식
```json
{
  "code": "VALIDATION_ERROR",
  "message": "..."
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
- 정책 서비스 단위 테스트
- 파일 업로드/다운로드 단위 테스트
- 컨트롤러 슬라이스 테스트
- PostgreSQL(Testcontainers) 통합테스트

## 배포 환경(PostgreSQL + S3)
필수 환경변수:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `S3_BUCKET`
- AWS 인증 기본 체인(`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION` 등)

예시:
```bash
DB_URL=jdbc:postgresql://localhost:5432/extension_blocker \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
S3_BUCKET=extension-blocker-files \
AWS_REGION=ap-northeast-2 \
./gradlew bootRun --args='--spring.profiles.active=postgres'
```

## 의도적으로 제외한 범위
- 로그인/권한/사용자별 정책
- 파일 삭제/버전 관리
- 관리자 전용 화면 분리
- 감사 로그/모니터링 고도화

자세한 메모는 [docs/assignment-notes.md](docs/assignment-notes.md) 참고.
