# 1단게: 빌드 스테이지
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle 래퍼와 호환성 캐싱을 위해 필수 파일만 우선 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 의존성 다운로드 시간 최소화를 위해 캐싱 (옵션)
RUN ./gradlew dependencies --no-daemon || true

# 소스코드 전체 복사 후, 테스트를 건너뛰고 실행 가능한 Jar 파일 빌드
COPY src src
RUN ./gradlew build -x test --no-daemon

# 2단계: 실행 스테이지 (가벼운 JRE 환경)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 타임존 세팅 (옵션)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# builder에서 생성된 Jar 파일만 복사
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# Spring Boot 기본 포트 노출
EXPOSE 8080

# Profile을 주입받아 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:local}", "-jar", "app.jar"]
