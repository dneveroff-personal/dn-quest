# DN Quest - Host-build Dockerfile (один на все сервисы)
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 dnquest && \
    adduser -D -s /bin/sh -u 1001 -G dnquest dnquest

WORKDIR /app

# ARG должен быть объявлен ДО использования в COPY
ARG SERVICE_NAME
ARG PORT=8080

COPY ${SERVICE_NAME}/build/libs/app.jar app.jar

RUN chown -R dnquest:dnquest /app && chmod +x app.jar

USER dnquest

EXPOSE ${PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]