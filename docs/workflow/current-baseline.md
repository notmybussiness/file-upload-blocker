# Current Baseline Snapshot

## API Contract (2026-03-03)

### Client API
- `POST /api/v1/client/files`
- `GET /api/v1/client/files`
- `GET /api/v1/client/files/{id}/download`

### Admin API
- `GET /api/v1/admin/extensions/policy`
- `PATCH /api/v1/admin/extensions/fixed/{name}`
- `POST /api/v1/admin/extensions/custom`
- `DELETE /api/v1/admin/extensions/custom/{id}`

### Removed Legacy Paths
- `/api/v1/files/**`
- `/api/v1/extensions/**`

### Error Shape
```json
{
  "code": "VALIDATION_ERROR",
  "message": "..."
}
```

## Data and Storage Structure

### `fixed_extension_policy`
- Purpose: 고정 확장자 상태(on/off)
- Columns: `name`(PK), `checked`, `updated_at`

### `custom_extension`
- Purpose: 관리자 커스텀 차단 확장자
- Columns: `id`(PK), `name`(unique), `created_at`

### `uploaded_file`
- Purpose: 업로드 파일 메타데이터
- Columns: `id`(UUID PK), `original_name`, `extension`, `content_type`, `size_bytes`, `storage_key`(unique), `created_at`

### Object Storage
- Purpose: 파일 본문(bytes)
- Key: `uploaded_file.storage_key`
- Local profile: `LocalFileObjectStorage`
- Postgres profile: `S3FileObjectStorage`

## Test Baseline (last green run)
- Total tests: 31
- Failures: 0
- Errors: 0
- Skipped: 0

### Unit
- `ExtensionPolicyServiceTest`: 6
- `FileServiceTest`: 5

### Controller Slice
- `ExtensionPolicyControllerTest`: 5
- `FileControllerTest`: 6

### Integration
- `ExtensionPolicyPostgresIntegrationTest`: 3
- `FileUploadPostgresIntegrationTest`: 3
- `ExtensionBlockerApplicationTests`: 3

### Coverage and E2E Policy Status
- Coverage percentage: 측정 미설정 (JaCoCo plugin/report task 없음)
- Browser E2E: 정식 기준선 미포함 (공식 리포트는 API 통합테스트 기준)
