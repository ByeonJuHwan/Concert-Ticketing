FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle bootJar --no-daemon

# 실행 이미지
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080 9093

ENV SERVER_PORT=8080
ENV GRPC_SERVER_PORT=9093

ENTRYPOINT ["java", "-jar", "app.jar"]