plugins {
    java
    org.springframework.boot.apply(false)
    io.spring.dependency-management.apply(false)
    org.flywaydb.flyway.apply(false)
    com.google.cloud.tools.jib.apply(false)
}

dependencies {
    implementation(project(":dn-quest-shared"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.flywaydb:flyway-core:9.22.3")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry:opentelemetry-api:1.32.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
    testImplementation("com.h2database:h2")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
        mavenBom("org.testcontainers:testcontainers-bom:1.19.3")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

flyway {
    url = "jdbc:postgresql://localhost:5432/dnquest_game_engine"
    user = "dn"
    password = "dn"
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
}

jib {
    from {
        image = "openjdk:21-jre-alpine"
    }
    to {
        image = "dn-quest/game-engine-service:1.0.0"
        tags = setOf("latest", "1.0.0")
    }
    container {
        ports = listOf("8084")
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
        format = "OCI"
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

springBoot {
    mainClass.set("dn.quest.gameengine.GameEngineServiceApplication")
}