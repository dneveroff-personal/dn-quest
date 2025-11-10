package dn.quest.shared.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultConsumerFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Health indicator для проверки подключения к Kafka
 */
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    private final String bootstrapServers;
    private final ConsumerFactory<String, String> consumerFactory;

    public KafkaHealthIndicator(String bootstrapServers, ConsumerFactory<String, String> consumerFactory) {
        this.bootstrapServers = bootstrapServers;
        this.consumerFactory = consumerFactory;
    }

    @Override
    public Health health() {
        try {
            // Проверяем подключение через AdminClient
            Health adminHealth = checkAdminClient();
            if (adminHealth.getStatus() != Health.Builder.up().build().getStatus()) {
                return adminHealth;
            }

            // Проверяем подключение через Consumer
            Health consumerHealth = checkConsumerConnection();
            if (consumerHealth.getStatus() != Health.Builder.up().build().getStatus()) {
                return consumerHealth;
            }

            return Health.up()
                    .withDetail("kafka", "Available")
                    .withDetail("bootstrapServers", bootstrapServers)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("kafka", "Unavailable")
                    .withDetail("bootstrapServers", bootstrapServers)
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }

    /**
     * Проверка подключения через AdminClient
     */
    private Health checkAdminClient() {
        try {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
            props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");

            try (AdminClient adminClient = AdminClient.create(props)) {
                DescribeClusterResult clusterResult = adminClient.describeCluster();
                String clusterId = clusterResult.clusterId().get();
                
                return Health.up()
                        .withDetail("adminClient", "Connected")
                        .withDetail("clusterId", clusterId)
                        .build();
            }
        } catch (InterruptedException | ExecutionException e) {
            return Health.down()
                    .withDetail("adminClient", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    /**
     * Проверка подключения через Consumer
     */
    private Health checkConsumerConnection() {
        try {
            Consumer<String, String> consumer = consumerFactory.createConsumer();
            
            // Пытаемся получить список топиков (простая проверка подключения)
            consumer.listTopics();
            consumer.close();
            
            return Health.up()
                    .withDetail("consumer", "Connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("consumer", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    /**
     * Создание ConsumerFactory для health check
     */
    public static ConsumerFactory<String, String> createHealthCheckConsumerFactory(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "health-check-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");

        return new DefaultConsumerFactory<>(props);
    }
}