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

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    // Feign Client
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-java8")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Shared module
    implementation(project(":dn-quest-shared"))
    
    // Micrometer for metrics
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
        mavenBom("org.testcontainers:testcontainers-bom:1.19.3")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

// Docker configuration
tasks.register<Copy>("unpack") {
    dependsOn(tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar"))
    from(zipTree(tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar").get().outputs.files.singleFile))
    into("build/dependency")
}

tasks.register<com.bmuschko.gradle.docker.tasks.image.Dockerfile>("dockerFile") {
    dependsOn(tasks.named<Copy>("unpack"))
    destFile.set(project.file("build/docker/Dockerfile"))
    from("build/dependency")
    into("build/dependency")
    instruction("FROM openjdk:17-jre-slim")
    instruction("WORKDIR /app")
    instruction("COPY build/dependency /app")
    instruction("ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]")
}

tasks.register<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuild") {
    dependsOn(tasks.named<com.bmuschko.gradle.docker.tasks.image.Dockerfile>("dockerFile"))
    images.add("${project.group}/${project.name}:${project.version}")
    images.add("${project.group}/${project.name}:latest")
}

// Jib configuration for containerization
jib {
    from {
        image = "openjdk:21-jre-alpine"
    }
    to {
        image = "dn-quest/team-management-service:1.0.0"
        tags = setOf("latest", "1.0.0")
    }
    container {
        ports = listOf("8085")
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
            "JAVA_OPTS" to "-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
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