# 1단계: 빌드
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle Wrapper 복사 (의존성 캐싱용)
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# 실행 권한 부여 + 의존성 다운로드 (캐싱 레이어)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 소스 복사 + 빌드
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 실행 (JRE만 포함 → 이미지 경량화)
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
