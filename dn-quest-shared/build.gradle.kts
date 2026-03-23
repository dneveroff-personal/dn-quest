plugins {
    `java-library`
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

group = "dn.quest"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Micrometer + Tracing
    api("io.micrometer:micrometer-core")
    api("io.micrometer:micrometer-registry-prometheus")
    api("io.micrometer:micrometer-tracing")
    api("io.micrometer:micrometer-tracing-bridge-brave")
    api("io.zipkin.reporter2:zipkin-reporter-brave")
    api("io.zipkin.brave:brave:5.16.0")

    // OpenTelemetry + Jaeger
    api("io.opentelemetry:opentelemetry-api")
    api("io.opentelemetry:opentelemetry-sdk")
    api("io.opentelemetry:opentelemetry-exporter-jaeger")
    api("io.opentelemetry:opentelemetry-exporter-jaeger-thrift")

    // Spring Kafka + Kafka clients
    api("org.springframework.kafka:spring-kafka")
    api("org.apache.kafka:kafka-clients")

    // Jakarta Annotation
    api("jakarta.annotation:jakarta.annotation-api")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Общие утилиты
    api("org.apache.commons:commons-lang3")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Тесты
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}