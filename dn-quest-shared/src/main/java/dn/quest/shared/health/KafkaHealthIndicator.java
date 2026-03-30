package dn.quest.shared.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.ConsumerFactory;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Health indicator для проверки подключения к Kafka
 */
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
            boolean adminOk = checkAdminClient();
            if (!adminOk) {
                return Health.down().withDetail("adminClient", "Failed").build();
            }

            // Проверяем подключение через Consumer
            boolean consumerOk = checkConsumerConnection();
            if (!consumerOk) {
                return Health.down().withDetail("consumer", "Failed").build();
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
    private boolean checkAdminClient() {
        try {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
            props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");

            try (AdminClient adminClient = AdminClient.create(props)) {
                DescribeClusterResult clusterResult = adminClient.describeCluster();
                String clusterId = clusterResult.clusterId().get();
                return clusterId != null;
            }
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    /**
     * Проверка подключения через Consumer
     */
    private boolean checkConsumerConnection() {
        try {
            var consumer = consumerFactory.createConsumer();
            consumer.listTopics();
            consumer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}