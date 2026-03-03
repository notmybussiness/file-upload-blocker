# Git Commit Sequence Standard

## Purpose
작업 히스토리를 설계 -> 구현 -> 검증 -> 문서 흐름으로 고정해 리뷰/회귀 분석을 빠르게 한다.

## Scope
- 기존 커밋 히스토리는 재작성하지 않는다.
- 다음 작업부터 동일 순서를 적용한다.
- 허용 prefix: `docs`, `feat`, `test`, `chore`, `refactor`, `fix`

## Standard Sequence
1. `docs: define <task> scope and constraints`
2. `feat: add <task> domain and schema changes`
3. `docs: snapshot <task> api contract`
4. `feat: implement <task> api handlers and service flow`
5. `feat: implement <task> frontend wiring`
6. `test: add <task> unit/slice/integration tests`
7. `docs: finalize <task> runbook and changelog`

## Stage Rules
- 커밋 전에 `git add -p`로 의도 단위만 스테이징한다.
- 커밋 직전 `git diff --staged`로 범위를 확인한다.
- 문서 커밋과 기능 커밋을 분리한다.
- API 변경이 있으면 API 계약 문서 커밋을 반드시 분리한다.

## Gate Commands
- API 커밋 직후: `./gradlew test --tests '*ControllerTest'`
- 테스트 커밋 직후: `./gradlew test`
- 배포/런타임 커밋 직후: `./gradlew build`

## Fixed Test Reporting Policy
- Unit: 서비스 규칙(성공/실패/경계)
- Slice: 컨트롤러 상태코드/응답 포맷
- Integration: DB + storage 경계 플로우
- E2E: 별도 브라우저 테스트가 없으면 "미구현"으로 명시
- Coverage: JaCoCo 미도입 시 "측정 미설정"으로 명시

## Infra/CI/CD Next Task Commit Blueprint
1. `docs: define infra and ci/cd scope for extension-blocker`
2. `feat: add deployment runtime configs and profile separation`
3. `docs: snapshot deployment interfaces and environment contract`
4. `feat: add ci pipeline for build and test`
5. `feat: add cd workflow and release strategy`
6. `test: add pipeline validation scenarios and smoke checks`
7. `docs: finalize github repository setup and operations guide`
