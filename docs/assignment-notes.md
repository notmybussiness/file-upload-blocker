# Assignment Notes

## 이 프로젝트에서 지킨 원칙

- 과제 MVP 외 기능은 넣지 않는다.
- 입력 검증 규칙과 에러 포맷을 고정해서 예측 가능한 동작만 제공한다.
- 인증/인가 없이 URL 경계로 Client/Admin 표면만 분리한다.
- 도메인 로직은 공유하고(서비스), API/화면만 분리한다.

## Client/Admin 분리 구조

1. FE

- Client 페이지: `/client` (`static/client/*`)
- Admin 페이지: `/admin` (`static/admin/*`)
- 루트 `/`는 `/client`로 리다이렉트

2. API

- Client API: `/api/v1/client/files/**`
- Admin API: `/api/v1/admin/extensions/**`
- 기존 공용 API(`/api/v1/files/**`, `/api/v1/extensions/**`)는 제거

3. 백엔드 계층

- 컨트롤러만 분리
  - `client.api.ClientFileController`
  - `admin.api.AdminExtensionPolicyController`
- 서비스는 공유
  - `FileService`, `ExtensionPolicyService`

## 업로드/다운로드 MVP 설계 메모

1. 저장소 분리

- 파일 메타데이터는 DB(`uploaded_file`)에 저장
- 파일 본문은 Object Storage 저장

2. 저장 방식 결정

- 로컬: `LocalFileObjectStorage` (디스크)
- 배포: `GcpCloudStorageService` (GCP Cloud Storage)

3. 메타데이터 스키마

- `id` (UUID)
- `original_name`
- `extension`
- `content_type`
- `size_bytes`
- `storage_key`
- `created_at`

4. 동작 순서

- 업로드 요청 수신
- 확장자 추출 및 차단 정책 확인
- 허용이면 저장소 put
- DB 메타데이터 save
- DB save 실패 시 저장소 delete(best effort)

## 서버 규칙 요약

- 커스텀 확장자 정규화: `trim -> lower-case`
- 허용 패턴: `^[a-z0-9]{1,20}$`
- 고정 확장자와 커스텀 이름 충돌 금지
- 커스텀 중복 금지
- 커스텀 최대 200개
- 파일 확장자 차단 시 업로드 자체를 거부

## 추가 고려사항 (이번 구현 범위 밖)

- 업로드 파일 삭제 API
- 사용자/조직 단위 정책 분리 + 인증/인가
- 변경 이력(audit) 저장
- 대용량 파일 스트리밍 최적화
