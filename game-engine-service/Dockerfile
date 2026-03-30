# Multi-stage build for optimized production image
FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="DN Quest Team"
LABEL description="DN Quest Game Engine Service"
LABEL version="1.0.0"

# Install required packages for building
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy gradle wrapper and configuration files
COPY gradlew .
COPY gradle gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first for better caching
RUN ./gradlew dependencies --configuration compileClasspath --no-daemon

# Copy source code
COPY dn-quest-shared/build.gradle.kts dn-quest-shared/build.gradle.kts
COPY dn-quest-shared/src dn-quest-shared/src
COPY game-engine-service/build.gradle.kts game-engine-service/build.gradle.kts
COPY game-engine-service/src game-engine-service/src

# Build the application
RUN ./gradlew :game-engine-service:bootJar --no-daemon -x test

# Production stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DN Quest Team"
LABEL description="DN Quest Game Engine Service"
LABEL version="1.0.0"

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 dnquest && \
    adduser -D -s /bin/sh -u 1001 -G dnquest dnquest

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/game-engine-service/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R dnquest:dnquest /app

# Switch to non-root user
USER dnquest

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/api/game/actuator/health || exit 1

# JVM options optimized for containers
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# Run the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]