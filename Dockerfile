# Multi-stage build для оптимизации размера образа
FROM openjdk:21-jdk-slim AS builder

# Устанавливаем необходимые зависимости для сборки
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копируем только необходимые файлы для сборки
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Делаем gradlew исполняемым
RUN chmod +x gradlew

# Собираем приложение
RUN ./gradlew clean build -x test --no-daemon

# Production stage
FROM openjdk:21-jre-slim

# Устанавливаем необходимые пакеты и создаем пользователя
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Копируем JAR из builder stage
COPY --from=builder /app/build/libs/dn-quest-*.jar app.jar

# Создаем директорию для логов
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Переключаемся на непривилегированного пользователя
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Настройки JVM для production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Запуск приложения (без debug агента для production)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]