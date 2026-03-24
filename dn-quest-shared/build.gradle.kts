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
    // Spring Boot
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-logging")

    // Micrometer + Tracing (Brave bridge - compatible with Spring Boot 3.2)
    api("io.micrometer:micrometer-core")
    api("io.micrometer:micrometer-registry-prometheus")
    api("io.micrometer:micrometer-tracing-bridge-brave:1.2.0")

    // Zipkin Reporter
    api("io.zipkin.reporter2:zipkin-reporter")
    api("io.zipkin.reporter2:zipkin-reporter-brave")

    // Spring Kafka + Kafka clients
    api("org.springframework.kafka:spring-kafka")
    api("org.apache.kafka:kafka-clients")

    // Jakarta Annotation
    api("jakarta.annotation:jakarta.annotation-api")

    // Logback
    api("ch.qos.logback:logback-classic")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Общие утилиты
    api("org.apache.commons:commons-lang3")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Validation
    api("jakarta.validation:jakarta.validation-api")

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

// Отключаем bootJar для общей библиотеки
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}