# 1. 빌드 단계
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY . .
COPY src/main/resources/firebase/athena-firebase-config.json ./athena-firebase-config.json
RUN ./gradlew clean build bootJar -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/athena-firebase-config.json ./athena-firebase-config.json
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]