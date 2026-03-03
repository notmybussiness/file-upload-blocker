## Summary
- 

## Commit Sequence Mapping
- [ ] `docs: define <task> scope and constraints`
- [ ] `feat: add <task> domain and schema changes`
- [ ] `docs: snapshot <task> api contract`
- [ ] `feat: implement <task> api handlers and service flow`
- [ ] `feat: implement <task> frontend wiring`
- [ ] `test: add <task> unit/slice/integration tests`
- [ ] `docs: finalize <task> runbook and changelog`

## API Contract
- API 문서 경로: 
- Breaking change 여부: `Yes/No`
- Error format 영향: `Yes/No`

## Validation / Test Evidence
- [ ] API 변경 직후: `./gradlew test --tests '*ControllerTest'`
- [ ] 테스트 커밋 직후: `./gradlew test`
- [ ] 배포 관련 변경 시: `./gradlew build`

## Test Report
- Unit:
- Slice:
- Integration:
- E2E: `Implemented / 미구현`
- Coverage: `수치 / 측정 미설정`

## Scope Control
- In scope:
- Out of scope:
- Rollback point:
