# ==============================================================================
# SkillBridge Backend — Production Multi-Stage Dockerfile (Spring Boot 3 / Java 21)
# ==============================================================================

# Stage 1: Build JAR with Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copy build configuration & wrapper
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build production bootJar without test execution
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Extract layers for optimal container caching (Spring Boot Layered JAR)
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination target/extracted

# Stage 2: Minimal Distroless / Hardened Alpine Runtime
FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app

# Install curl for healthcheck & create unprivileged runtime user
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 spring \
    && useradd --system --uid 10001 --gid spring --no-create-home spring

# Copy extracted layers from builder
COPY --from=builder --chown=spring:spring /workspace/target/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/target/extracted/application/ ./

# Hardened security constraints
USER spring:spring
EXPOSE 8080

# Production JVM options: CGroup-aware memory sizing & GC tuning
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

# Health check against Spring Actuator
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f -s http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
