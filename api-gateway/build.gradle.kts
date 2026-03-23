plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.cloud.tools.jib") version "3.4.0"
}

group = "dn.quest"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.0"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Cloud Gateway
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // Spring Cloud LoadBalancer
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry:opentelemetry-api")

    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

    // Shared library
    implementation(project(":dn-quest-shared"))

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Development tools
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

// === Main class for Spring Boot (правильный способ в Kotlin DSL) ===
tasks.bootJar {
    mainClass.set("dn.quest.gateway.ApiGatewayApplication")
}

// Jib configuration for Docker image building
jib {
    from {
        image = "openjdk:21-jre-alpine"
    }
    to {
        image = "dn-quest/api-gateway:1.0.0"
        tags = setOf("latest", "1.0.0")
    }
    container {
        ports = listOf("8080")
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

tasks.withType<Test> {
    useJUnitPlatform()
}