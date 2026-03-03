# Assignment Notes

## 이 프로젝트에서 지킨 원칙
- 과제 MVP 외 기능은 넣지 않는다.
- 입력 검증 규칙과 에러 포맷을 고정해서 예측 가능한 동작만 제공한다.
- 화면은 바닐라 JS로 단순하게 구성한다.

## 업로드/다운로드 MVP 설계 메모
1. 저장소 분리
- 파일 메타데이터는 DB(`uploaded_file`)에 저장
- 파일 본문은 Object Storage 저장

2. 저장 방식 결정
- 로컬: `LocalFileObjectStorage` (디스크)
- 배포: `S3FileObjectStorage`

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
- 사용자/조직 단위 정책 분리
- 변경 이력(audit) 저장
- 대용량 파일 스트리밍 최적화
