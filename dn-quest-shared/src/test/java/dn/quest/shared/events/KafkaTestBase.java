package dn.quest.shared.events;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Базовый класс для интеграционных тестов с Kafka
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, 
               brokerProperties = {
                   "listeners=PLAINTEXT://localhost:9092",
                   "port=9092"
               })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class KafkaTestBase {

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    protected KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    protected KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    protected static final String TEST_TOPIC = "test-topic";
    protected static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    @BeforeEach
    void setUp() {
        // Ожидаем запуска встроенного Kafka брокера
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        // Останавливаем все слушатели после каждого теста
        kafkaListenerEndpointRegistry.getAllListenerContainers().forEach(container -> {
            container.stop();
            container.getContainerProperties().setRunning(false);
        });
    }

    /**
     * Отправка тестового события в Kafka
     */
    protected void sendTestEvent(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
        kafkaTemplate.flush();
    }

    /**
     * Ожидание обработки сообщений
     */
    protected void waitForMessageProcessing() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1000);
    }

    /**
     * Получение списка топиков для тестирования
     */
    protected String[] getTestTopics() {
        return new String[]{
            KafkaTopics.USER_EVENTS,
            KafkaTopics.QUEST_EVENTS,
            KafkaTopics.GAME_EVENTS,
            KafkaTopics.TEAM_EVENTS,
            KafkaTopics.FILE_EVENTS,
            KafkaTopics.NOTIFICATION_EVENTS
        };
    }
}