plugins {
    id("java")
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("org.flywaydb.flyway") version "9.22.3" apply false
    id("com.google.cloud.tools.jib") version "3.4.0" apply false
    id("jacoco")
    kotlin("jvm") version "1.9.20" apply false
    kotlin("plugin.spring") version "1.9.20" apply false
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
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    maven { url = uri("https://repo1.maven.org/maven2/") }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")

    group = "dn.quest"
    version = "1.0.0"

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
        
        // Cloud
        implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0")
        
        // Monitoring
        implementation("io.micrometer:micrometer-registry-prometheus")
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

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-unchecked"))
    }

    jacoco {
        toolVersion = "0.8.10"
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

// Docker build task for all services
tasks.register("buildAllDockerImages") {
    group = "build"
    description = "Builds Docker images for all services"
    
    dependsOn(
        ":authentication-service:jib",
        ":user-management-service:jib",
        ":quest-management-service:jib",
        ":game-engine-service:jib",
        ":team-management-service:jib",
        ":notification-service:jib",
        ":statistics-service:jib",
        ":file-storage-service:jib",
        ":api-gateway:jib"
    )
}

// Clean task for all subprojects
tasks.named("clean") {
    dependsOn(subprojects.map { "${it.path}:clean" })
}