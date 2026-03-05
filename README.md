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

- 파일 본문: Object Storage (`GCP Cloud Storage`)
- 파일 메타데이터: `PostgreSQL`

로컬 개발 편의를 위해 `local` 프로파일은 디스크 저장소를 사용하고,
배포(`prod` 프로파일)에서는 GCP Cloud Storage를 사용하도록 분리했습니다.

## 기술 스택

- Java 21
- Spring Boot 4
- Spring Data JPA
- Flyway
- H2 (local 실행)
- PostgreSQL (핵심 통합테스트/배포 대상)
- Google Cloud Storage (배포 저장소)
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

## CI 파이프라인 (GitHub Actions)

`push` 또는 `PR`이 `main` 브랜치로 올 때 자동 실행됩니다.

| Stage             | 내용                                    |
| ----------------- | --------------------------------------- |
| Build & Unit Test | Gradle 빌드 + 단위/슬라이스 테스트 (H2) |
| Integration Test  | Testcontainers PostgreSQL 통합테스트    |

## 배포 환경 (GCP VM + Docker Compose + Cloud Storage)

- **서버**: GCP Compute Engine (`e2-small`, asia-northeast3)
- **앱 + DB**: Docker Compose (app + PostgreSQL 컨테이너)
- **파일 저장소**: GCP Cloud Storage (네이티브 `google-cloud-storage` 라이브러리)
- **인증**: 서비스 계정 JSON 키 (`GOOGLE_APPLICATION_CREDENTIALS`)

### 실행 방법

1. GCP 서비스 계정 JSON 키(`gcp-credentials.json`)를 프로젝트 루트에 배치합니다.
2. `.env` 파일을 생성합니다:

```env
POSTGRES_USER=admin
POSTGRES_PASSWORD=<비밀번호>
GCP_PROJECT_ID=<프로젝트ID>
GCP_BUCKET_NAME=<버킷이름>
```

3. 배포:

```bash
docker-compose up -d --build
```

접속:

- 라이브 서비스: `http://34.22.109.226/client`

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
