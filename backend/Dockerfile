FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew 2>/dev/null || true
RUN gradle build -x test --no-daemon 2>/dev/null || true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
EXPOSE 8080
COPY --from=builder /app/build/libs/*.jar app.jar 2>/dev/null || true
ENTRYPOINT ["java", "-jar", "app.jar"]
