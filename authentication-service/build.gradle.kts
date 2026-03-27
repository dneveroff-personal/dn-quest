plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.flywaydb.flyway") version "9.22.3"
    id("com.google.cloud.tools.jib") version "3.4.0"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/central") }
}

dependencies {
    // Shared library
    implementation(project(":dn-quest-shared"))
    
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.flywaydb:flyway-core:9.22.3")
    
    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    // Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    
    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.opentelemetry:opentelemetry-api:1.32.0")
    
    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    
    // Utilities
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
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

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

// Flyway configuration
flyway {
    url = "jdbc:postgresql://localhost:5432/dnquest_auth"
    user = "dn"
    password = "dn"
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
}

// Jib configuration for Docker image building
jib {
    from {
        image = "openjdk:21-jre-alpine"
    }
    to {
        image = "dn-quest/authentication-service"
        tags = setOf("latest", "1.0.0")
    }
    container {
        ports = listOf("8081")
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
    mainClass.set("dn.quest.authentication.AuthenticationServiceApplication")
}
