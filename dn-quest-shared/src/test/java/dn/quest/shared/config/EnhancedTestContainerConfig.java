package dn.quest.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Улучшенная конфигурация TestContainers для комплексного тестирования
 * с поддержкой PostgreSQL, Redis, Kafka, MinIO, Elasticsearch и RabbitMQ
 */
@Slf4j
@TestConfiguration
@Testcontainers
public class EnhancedTestContainerConfig {

    // PostgreSQL контейнер
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dnquest_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(60));

    // Redis контейнер
    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(30))
            .waitingFor(Wait.forListeningPort());

    // Kafka контейнер
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(90))
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");

    // MinIO контейнер для S3 совместимого хранилища
    @Container
    static GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
            .withExposedPorts(9000, 9001)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data --console-address ':9001'")
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(60))
            .waitingFor(Wait.forHttp("/minio/health/live").forPort(9000));

    // Elasticsearch контейнер
    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.0"))
            .withPassword("elastic")
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(120))
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node");

    // RabbitMQ контейнер
    @Container
    static GenericContainer<?> rabbitmq = new GenericContainer<>(DockerImageName.parse("rabbitmq:3.12-management-alpine"))
            .withExposedPorts(5672, 15672)
            .withEnv("RABBITMQ_DEFAULT_USER", "guest")
            .withEnv("RABBITMQ_DEFAULT_PASS", "guest")
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(60))
            .waitingFor(Wait.forLogMessage(".*Server startup complete.*", 1));

    // Prometheus контейнер для метрик
    @Container
    static GenericContainer<?> prometheus = new GenericContainer<>(
            DockerImageName.parse("prom/prometheus:latest"))
            .withExposedPorts(9090)
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(30))
            .withFileSystemBind("src/test/resources/prometheus.yml", "/etc/prometheus/prometheus.yml");

    // Grafana контейнер для визуализации метрик
    @Container
    static GenericContainer<?> grafana = new GenericContainer<>(
            DockerImageName.parse("grafana/grafana:latest"))
            .withExposedPorts(3000)
            .withEnv("GF_SECURITY_ADMIN_PASSWORD", "admin")
            .withReuse(true)
            .withStartupTimeout(Duration.ofSeconds(30));

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
        return redis.getMappedPort(6379);
    }

    /**
     * Получение эндпоинта S3 (MinIO)
     */
    @Bean
    @Primary
    public String s3Endpoint() {
        return String.format("http://%s:%d", minio.getHost(), minio.getMappedPort(9000));
    }

    /**
     * Получение региона S3
     */
    @Bean
    @Primary
    public String s3Region() {
        return "us-east-1";
    }

    /**
     * Получение access key для S3
     */
    @Bean
    @Primary
    public String s3AccessKey() {
        return "minioadmin";
    }

    /**
     * Получение secret key для S3
     */
    @Bean
    @Primary
    public String s3SecretKey() {
        return "minioadmin";
    }

    /**
     * Получение имени бакета для тестов
     */
    @Bean
    @Primary
    public String s3BucketName() {
        return "dn-quest-test-files";
    }

    /**
     * Получение URL Elasticsearch
     */
    @Bean
    @Primary
    public String elasticsearchUrl() {
        return elasticsearch.getHttpHostAddress();
    }

    /**
     * Получение хоста RabbitMQ
     */
    @Bean
    @Primary
    public String rabbitmqHost() {
        return rabbitmq.getHost();
    }

    /**
     * Получение порта RabbitMQ
     */
    @Bean
    @Primary
    public Integer rabbitmqPort() {
        return rabbitmq.getMappedPort(5672);
    }

    /**
     * Получение URL управления RabbitMQ
     */
    @Bean
    @Primary
    public String rabbitmqManagementUrl() {
        return String.format("http://%s:%d", rabbitmq.getHost(), rabbitmq.getMappedPort(15672));
    }

    /**
     * Получение URL Prometheus
     */
    @Bean
    @Primary
    public String prometheusUrl() {
        return String.format("http://%s:%d", prometheus.getHost(), prometheus.getMappedPort(9090));
    }

    /**
     * Получение URL Grafana
     */
    @Bean
    @Primary
    public String grafanaUrl() {
        return String.format("http://%s:%d", grafana.getHost(), grafana.getMappedPort(3000));
    }

    /**
     * Проверка готовности всех контейнеров
     */
    @Bean
    @Primary
    public boolean containersReady() {
        boolean allReady = postgres.isRunning() && 
                          redis.isRunning() && 
                          kafka.isRunning() && 
                          minio.isRunning() && 
                          elasticsearch.isRunning() && 
                          rabbitmq.isRunning() &&
                          prometheus.isRunning() &&
                          grafana.isRunning();

        if (allReady) {
            log.info("All test containers are ready:");
            log.info("  PostgreSQL: {}:{}", postgres.getHost(), postgres.getMappedPort(5432));
            log.info("  Redis: {}:{}", redis.getHost(), redis.getMappedPort(6379));
            log.info("  Kafka: {}", kafka.getBootstrapServers());
            log.info("  MinIO: {}:{}", minio.getHost(), minio.getMappedPort(9000));
            log.info("  Elasticsearch: {}", elasticsearch.getHttpHostAddress());
            log.info("  RabbitMQ: {}:{}", rabbitmq.getHost(), rabbitmq.getMappedPort(5672));
            log.info("  Prometheus: {}:{}", prometheus.getHost(), prometheus.getMappedPort(9090));
            log.info("  Grafana: {}:{}", grafana.getHost(), grafana.getMappedPort(3000));
        } else {
            log.warn("Some containers are not ready:");
            logContainerStatus("PostgreSQL", postgres.isRunning());
            logContainerStatus("Redis", redis.isRunning());
            logContainerStatus("Kafka", kafka.isRunning());
            logContainerStatus("MinIO", minio.isRunning());
            logContainerStatus("Elasticsearch", elasticsearch.isRunning());
            logContainerStatus("RabbitMQ", rabbitmq.isRunning());
            logContainerStatus("Prometheus", prometheus.isRunning());
            logContainerStatus("Grafana", grafana.isRunning());
        }

        return allReady;
    }

    private void logContainerStatus(String name, boolean running) {
        log.info("  {}: {}", name, running ? "RUNNING" : "STOPPED");
    }

    /**
     * Конфигурация для создания тестовых бакетов в MinIO
     */
    @Bean
    @Primary
    public TestBucketInitializer testBucketInitializer() {
        return new TestBucketInitializer();
    }

    /**
     * Инициализатор тестовых бакетов
     */
    public static class TestBucketInitializer {
        public void initializeBuckets(String endpoint, String accessKey, String secretKey, String bucketName) {
            // Здесь можно добавить логику для создания бакетов в MinIO
            // Например, используя AWS SDK для Java
            log.info("Initializing test bucket: {} in {}", bucketName, endpoint);
        }
    }

    /**
     * Конфигурация для создания тестовых индексов в Elasticsearch
     */
    @Bean
    @Primary
    public TestIndexInitializer testIndexInitializer() {
        return new TestIndexInitializer();
    }

    /**
     * Инициализатор тестовых индексов
     */
    public static class TestIndexInitializer {
        public void initializeIndices(String elasticsearchUrl) {
            // Здесь можно добавить логику для создания индексов в Elasticsearch
            log.info("Initializing test indices in: {}", elasticsearchUrl);
        }
    }

    /**
     * Конфигурация для создания тестовых очередей в RabbitMQ
     */
    @Bean
    @Primary
    public TestQueueInitializer testQueueInitializer() {
        return new TestQueueInitializer();
    }

    /**
     * Инициализатор тестовых очередей
     */
    public static class TestQueueInitializer {
        public void initializeQueues(String host, int port) {
            // Здесь можно добавить логику для создания очередей в RabbitMQ
            log.info("Initializing test queues in {}:{}", host, port);
        }
    }
}