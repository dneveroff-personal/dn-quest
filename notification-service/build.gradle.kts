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
    implementation(project(":dn-quest-shared"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.flywaydb:flyway-core:9.22.3")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry:opentelemetry-api:1.32.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    
    // JAXB support for Java 11+ - use javax namespace for backward compatibility
    // This is needed because jackson-module-jaxb-annotations still uses javax.xml.bind
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.9")
    implementation("javax.activation:activation:1.1.1")
    
    // javax.annotation meta-annotations (provides javax.annotation.meta.When)
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")
    
    // Telegram
    implementation("org.telegram:telegrambots:6.9.7.1")
    
    // Thymeleaf
    implementation("org.thymeleaf:thymeleaf-spring6:3.1.1.RELEASE")
    
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

flyway {
    url = "jdbc:postgresql://localhost:5432/dnquest_notifications"
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
    archiveFileName.set("app.jar")
}

tasks.jar {
    enabled = false
}
