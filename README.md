# 파일 확장자 차단 과제 MVP

과제 목적에 맞춘 최소 구현(MVP)입니다.
정책 관리(Admin)와 업로드/다운로드(Client)를 분리했고, 과한 확장 설계는 의도적으로 제외했습니다.

## 구현 기능

- Admin 화면에서 고정 확장자 7개(`bat`, `cmd`, `com`, `cpl`, `exe`, `scr`, `js`) 체크/해제 + DB 저장 + 새로고침 유지
- Admin 화면에서 커스텀 확장자 추가/삭제 + DB 반영
- 커스텀 입력 길이 20자 제한
- 커스텀 최대 200개 제한
- 커스텀 중복 방지(대소문자 무시)
- Client 화면에서 파일 업로드 API(`multipart/form-data`) + 차단 정책 검증
- Client 화면에서 업로드 목록 조회 + 다운로드

## 화면 분리 (동일 서버, 2개 페이지)

- Client: `GET /client`
- Admin: `GET /admin`
- 루트 `/`는 `/client`로 리다이렉트

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
- Vanilla JS (Client/Admin 2개 정적 페이지)

## API

### Client API

- `POST /api/v1/client/files`
- `GET /api/v1/client/files`
- `GET /api/v1/client/files/{id}/download`

### Admin API

- `GET /api/v1/admin/extensions/policy`
- `PATCH /api/v1/admin/extensions/fixed/{name}`
  - body: `{ "checked": true | false }`
- `POST /api/v1/admin/extensions/custom`
  - body: `{ "name": "sh" }`
- `DELETE /api/v1/admin/extensions/custom/{id}`

### 제거된 레거시 API

- `/api/v1/files/**`
- `/api/v1/extensions/**`

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

- Client 화면: `http://localhost:8080/client`
- Admin 화면: `http://localhost:8080/admin`
- H2 Console: `http://localhost:8080/h2-console`

## 테스트 실행

```bash
./gradlew test
```

포함 테스트:

- 정책 서비스 단위 테스트
- 파일 업로드/다운로드 단위 테스트
- Client/Admin 컨트롤러 슬라이스 테스트
- PostgreSQL(Testcontainers) 통합테스트
- 레거시 API 404 검증 테스트

## 배포 환경 (단일 VM + Docker Compose + GCP Storage)

운영계(Production) 배포 시 손쉬운 인프라 프로비저닝을 위해 `docker-compose`를 지원합니다.
API(백엔드)와 정적 화면(프론트엔드)은 `app` 단일 컨테이너로 실행되며, 데이터베이스는 내장 `postgres` 컨테이너로 띄워 유지보수와 비용을 절감합니다. 반면 "업로드된 파일"과 같은 상태(State) 데이터는 외부 객체 스토리지(`GCP Cloud Storage`)로 연동하여 클라우드 네이티브 원칙을 만족합니다.

### 실행 방법

1. GCP 인증 키(`gcp-credentials.json`) 발급 후 프로젝트 루트 디렉토리에 위치시킵니다.
2. `src/main/resources/application-secret.yml.template`을 참고하여 `application-secret.yml`을 작성 후 DB 패스워드를 기입합니다.
3. 아래 명령어로 백그라운드 통합 배포를 시작합니다.

```bash
docker-compose up -d --build
```

접속:

- 통합 서비스: `http://{서버_외부_IP}:80/client` (기본 80 포트로 매핑됨)

## 의도적으로 제외한 범위

- 로그인/권한/사용자별 정책
- 파일 삭제/버전 관리
- 감사 로그/모니터링 고도화

자세한 메모는 `docs/assignment-notes.md` 참고.

## Git 커밋 워크플로우 표준

- 표준 문서: `docs/workflow/git-commit-standard.md`
- 기준선 스냅샷: `docs/workflow/current-baseline.md`
- PR 체크리스트 템플릿: `.github/PULL_REQUEST_TEMPLATE.md`
- 커밋 메시지 템플릿: `.gitmessage`

로컬 git에 커밋 템플릿을 연결하려면:

```bash
git config commit.template .gitmessage
```
