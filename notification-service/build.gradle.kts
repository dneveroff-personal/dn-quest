plugins {
    java

    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.cloud.tools.jib") version "3.4.0"
}

dependencies {
    implementation(project(":dn-quest-shared"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry:opentelemetry-api:1.32.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")
    
    // Telegram
    implementation("org.telegram:telegrambots:6.9.7.1")
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jib {
    from {
        image = "openjdk:21-jre-alpine"
    }
    to {
        image = "dn-quest/notification-service"
        tags = setOf("latest", "1.0.0")
    }
    container {
        ports = listOf("8086")
        jvmFlags = listOf(
            "-Xms256m",
            "-Xmx512m",
            "-XX:+UseG1GC",
            "-XX:+UseContainerSupport",
            "-XX:MaxRAMPercentage=75.0",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+UseCGroupMemoryLimitForHeap"
        )
        environment = mapOf(
            "JAVA_OPTS" to "-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0",
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
        user = "1001:1001"
        creationTime = "USE_CURRENT_TIMESTAMP"
        format = com.google.cloud.tools.jib.api.buildplan.ImageFormat.OCI
    }
    extraDirectories {
        paths {
            path {
                setFrom(file("src/main/jib"))
            }
        }
    }
    containerizingMode = "packaged"
}

tasks.bootJar {
    mainClass.set("dn.quest.notification.NotificationServiceApplication")
}
