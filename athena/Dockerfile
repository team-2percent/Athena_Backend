# 1. 빌드 단계
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build bootJar -x test

# Firebase config 복사 (resources 경로 유지)
RUN mkdir -p /app/src/main/resources/firebase
COPY src/main/resources/firebase/athena-firebase-config.json /app/src/main/resources/firebase/athena-firebase-config.json

# 2. 실행 단계
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/src/main/resources/firebase/athena-firebase-config.json /app/src/main/resources/firebase/athena-firebase-config.json
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]