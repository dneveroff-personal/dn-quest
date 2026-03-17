package dn.quest.shared.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Конфигурация TestContainers для интеграционных тестов
 */
@TestConfiguration
@Testcontainers
public class TestContainerConfig {

    // PostgreSQL контейнер
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dnquest_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    // Redis контейнер
    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
            .withReuse(true);

    // Kafka контейнер
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true);

    // LocalStack для S3 (MinIO)
    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withReuse(true);

    /**
     * Получение URL для подключения к PostgreSQL
     */
    @Bean
    @Primary
    public String testDatabaseUrl() {
        return postgres.getJdbcUrl();
    }

    /**
     * Получение хоста Kafka
     */
    @Bean
    @Primary
    public String kafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }

    /**
     * Получение хоста Redis
     */
    @Bean
    @Primary
    public String redisHost() {
        return redis.getHost();
    }

    /**
     * Получение порта Redis
     */
    @Bean
    @Primary
    public Integer redisPort() {
        return redis.getFirstMappedPort();
    }

    /**
     * Получение эндпоинта S3
     */
    @Bean
    @Primary
    public String s3Endpoint() {
        return localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString();
    }

    /**
     * Получение региона S3
     */
    @Bean
    @Primary
    public String s3Region() {
        return localStack.getRegion();
    }

    /**
     * Получение access key для S3
     */
    @Bean
    @Primary
    public String s3AccessKey() {
        return localStack.getAccessKey();
    }

    /**
     * Получение secret key для S3
     */
    @Bean
    @Primary
    public String s3SecretKey() {
        return localStack.getSecretKey();
    }
}