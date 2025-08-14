FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/quiz-engine-*.jar app.jar
# агент нужен для работы дебага
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
