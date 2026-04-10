# Руководство по тестированию DN Quest

Это комплексное руководство по тестированию микросервисной архитектуры DN Quest, охватывающее все аспекты тестирования от unit тестов до нагрузочного тестирования.

## Содержание

1. [Обзор архитектуры тестирования](#обзор-архитектуры-тестирования)
2. [Типы тестов](#типы-тестов)
3. [Настройка окружения](#настройка-окружения)
4. [Запуск тестов](#запуск-тестов)
5. [Написание тестов](#написание-тестов)
6. [TestContainers](#testcontainers)
7. [Нагрузочное тестирование](#нагрузочное-тестирование)
8. [CI/CD интеграция](#cicd-интеграция)
9. [Отчеты о тестировании](#отчеты-о-тестировании)
10. [Лучшие практики](#лучшие-практики)

## Обзор архитектуры тестирования

DN Quest использует многоуровневую архитектуру тестирования:

```
┌─────────────────────────────────────────────────────────────┐
│                    E2E Тесты                                │
│  (Полный пользовательский путь через все сервисы)           │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                Интеграционные тесты                         │
│  (Тестирование взаимодействия между микросервисами)        │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                  Unit тесты                                 │
│  (Тестирование отдельных компонентов и сервисов)           │
└─────────────────────────────────────────────────────────────┘
```

### Структура тестовых директорий

```
dn-quest/
├── dn-quest-shared/src/test/
│   ├── java/dn/quest/shared/
│   │   ├── base/                    # Базовые классы для тестов
│   │   ├── config/                  # Тестовые конфигурации
│   │   ├── util/                    # Утилиты для тестов
│   │   ├── e2e/                     # End-to-end тесты
│   │   ├── load/                    # Нагрузочные тесты
│   │   └── integration/             # Общие интеграционные тесты
│   └── resources/
│       ├── application-test.yml     # Тестовая конфигурация
│       └── testcontainers/          # Конфигурации TestContainers
├── {service}/src/test/java/
│   └── dn/quest/{service}/          # Тесты конкретного сервиса
├── scripts/                         # Скрипты для запуска тестов
├── test-logs/                       # Логи выполнения тестов
└── test-reports/                    # Отчеты о тестировании
```

## Типы тестов

### 1. Unit тесты

Unit тесты проверяют отдельные компоненты в изоляции:

- **Цель**: Проверка логики отдельных классов и методов
- **Скорость**: Очень быстрые
- **Изоляция**: Полная изоляция с использованием моков
- **Примеры**: Тесты сервисов, репозиториев, утилит

```java
@ExtendWith(MockitoExtension.class)
class QuestServiceTest {
    @Mock
    private QuestRepository questRepository;
    
    @InjectMocks
    private QuestServiceImpl questService;
    
    @Test
    void shouldCreateQuest() {
        // Given
        Quest quest = new Quest();
        quest.setTitle("Test Quest");
        
        when(questRepository.save(any(Quest.class))).thenReturn(quest);
        
        // When
        Quest result = questService.createQuest(quest);
        
        // Then
        assertThat(result.getTitle()).isEqualTo("Test Quest");
        verify(questRepository).save(quest);
    }
}
```

### 2. Интеграционные тесты

Интеграционные тесты проверяют взаимодействие между компонентами:

- **Цель**: Проверка взаимодействия с базой данных, Kafka, другими сервисами
- **Скорость**: Средние
- **Изоляция**: Частичная изоляция с TestContainers
- **Примеры**: Тесты контроллеров, репозиториев с реальной БД

```java
@SpringBootTest
@Testcontainers
class QuestControllerIntegrationTest extends AbstractIntegrationTestBase {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateQuestThroughApi() {
        // Given
        CreateQuestRequest request = new CreateQuestRequest();
        request.setTitle("Integration Test Quest");
        
        // When
        ResponseEntity<QuestResponse> response = restTemplate.postForEntity(
            "/api/quests", request, QuestResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getTitle()).isEqualTo("Integration Test Quest");
    }
}
```

### 3. End-to-End тесты

E2E тесты проверяют полный пользовательский путь:

- **Цель**: Проверка полного сценария использования
- **Скорость**: Медленные
- **Изоляция**: Минимальная, полная система
- **Примеры**: Регистрация → Создание квеста → Игра → Завершение

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserJourneyE2ETest extends AbstractIntegrationTestBase {
    
    @Test
    @Order(1)
    void testCompleteUserJourney() {
        // Регистрация пользователя
        registerUser();
        
        // Создание квеста
        createQuest();
        
        // Начало игровой сессии
        startGameSession();
        
        // Завершение игры
        completeGameSession();
        
        // Проверка результатов
        verifyUserStatistics();
    }
}
```

### 4. Нагрузочные тесты

Нагрузочные тесты проверяют производительность системы:

- **Цель**: Проверка производительности и масштабируемости
- **Скорость**: Очень медленные
- **Изоляция**: Продуктивное окружение
- **Примеры**: Тесты пропускной способности, времени отклика

## Настройка окружения

### Требования

- Java 17+
- Docker & Docker Compose
- Gradle 7.0+
- PostgreSQL 14+
- Redis 6+
- Kafka 2.8+

### Конфигурация тестовых профилей

#### application-test.yml
```yaml
spring:
  profiles:
    active: test
  
  datasource:
    url: jdbc:tc:postgresql:14-alpine:///dnquest_test
    username: test
    password: test
    
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka-dev:29092}
    consumer:
      group-id: test-group
      auto-offset-reset: earliest
      
  redis:
    host: localhost
    port: 6379
    database: 1

logging:
  level:
    dn.quest: DEBUG
    org.testcontainers: INFO
```

#### application-load-test.yml
```yaml
spring:
  profiles:
    active: load-test
    
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      
  kafka:
    producer:
      batch-size: 16384
      linger-ms: 5
      
logging:
  level:
    dn.quest: INFO
```

### TestContainers конфигурация

```java
@TestConfiguration
public class TestContainerConfig {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("dnquest_test")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"));
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
            .withExposedPorts(6379);
    
    static {
        postgres.start();
        kafka.start();
        redis.start();
        
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
        System.setProperty("spring.redis.host", redis.getHost());
        System.setProperty("spring.redis.port", redis.getMappedPort(6379).toString());
    }
}
```

## Запуск тестов

### Скрипты для запуска

#### Все тесты
```bash
# Запуск всех тестов
./scripts/run-all-tests.sh

# С опциями
./scripts/run-all-tests.sh --skip-e2e --parallel --profile=test
```

#### Интеграционные тесты
```bash
# Запуск интеграционных тестов
./scripts/run-integration-tests.sh

# С опциями
./scripts/run-integration-tests.sh --no-parallel --services=api-gateway,quest-management
```

#### Нагрузочные тесты
```bash
# Запуск нагрузочных тестов
./scripts/run-load-tests.sh

# С опциями
./scripts/run-load-tests.sh --concurrent-users=100 --test-duration=600 --scenarios=api-gateway,kafka-events
```

### Gradle команды

```bash
# Unit тесты
./gradlew test

# Интеграционные тесты
./gradlew integrationTest

# E2E тесты
./gradlew e2eTest

# Нагрузочные тесты
./gradlew loadTest

# Все тесты
./gradlew check

# Тесты с покрытием
./gradlew test jacocoTestReport
```

### Запуск конкретных тестов

```bash
# Конкретный класс
./gradlew test --tests "dn.quest.quest.service.QuestServiceTest"

# Пакет
./gradlew test --tests "dn.quest.quest.service.*"

# Метод
./gradlew test --tests "dn.quest.quest.service.QuestServiceTest.shouldCreateQuest"
```

## Написание тестов

### Базовые классы

#### AbstractIntegrationTestBase
```java
@SpringBootTest
@Testcontainers
@Transactional
public abstract class AbstractIntegrationTestBase {
    
    @Autowired
    protected TestRestTemplate restTemplate;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Настройка TestContainers
    }
    
    @BeforeEach
    void setUp() {
        // Общая настройка для всех тестов
    }
    
    @AfterEach
    void tearDown() {
        // Очистка после тестов
    }
    
    protected void cleanupTestData() {
        // Очистка тестовых данных
    }
}
```

#### AbstractLoadTestBase
```java
public abstract class AbstractLoadTestBase {
    
    protected final AtomicInteger successCount = new AtomicInteger(0);
    protected final AtomicInteger failureCount = new AtomicInteger(0);
    protected final Map<String, Double> metrics = new ConcurrentHashMap<>();
    
    protected void recordSuccess() {
        successCount.incrementAndGet();
    }
    
    protected void recordFailure() {
        failureCount.incrementAndGet();
    }
    
    protected void recordMetric(String name, double value) {
        metrics.put(name, value);
    }
    
    protected double getSuccessRate() {
        int total = successCount.get() + failureCount.get();
        return total > 0 ? (double) successCount.get() / total * 100 : 0;
    }
}
```

### TestDataFactory

```java
public class TestDataFactory {
    
    public static Quest createQuest() {
        Quest quest = new Quest();
        quest.setTitle("Test Quest");
        quest.setDescription("Test Description");
        quest.setDifficulty(QuestDifficulty.EASY);
        quest.setEstimatedDuration(30);
        quest.setCategory(QuestCategory.ADVENTURE);
        return quest;
    }
    
    public static User createUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.PLAYER);
        return user;
    }
    
    public static CreateQuestRequest createCreateQuestRequest() {
        CreateQuestRequest request = new CreateQuestRequest();
        request.setTitle("Test Quest");
        request.setDescription("Test Description");
        request.setDifficulty("EASY");
        request.setEstimatedDuration(30);
        request.setCategory("ADVENTURE");
        return request;
    }
}
```

### Mock конфигурации

```java
@TestConfiguration
public class TestMockConfig {
    
    @Bean
    @Primary
    public EmailService mockEmailService() {
        return Mockito.mock(EmailService.class);
    }
    
    @Bean
    @Primary
    public FileStorageService mockFileStorageService() {
        FileStorageService mock = Mockito.mock(FileStorageService.class);
        when(mock.storeFile(any())).thenReturn("test-file-id");
        return mock;
    }
}
```

## TestContainers

### Поддерживаемые контейнеры

- PostgreSQL 14
- Redis 6
- Kafka 2.8
- MinIO (S3 совместимое хранилище)
- Elasticsearch 7
- RabbitMQ 3.9

### Пример использования

```java
@Testcontainers
class FileStorageIntegrationTest {
    
    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withUserName("accesskey")
            .withPassword("secretkey");
    
    @Test
    void shouldStoreFileInMinIO() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "content".getBytes()
        );
        
        // When
        String fileId = fileStorageService.storeFile(file);
        
        // Then
        assertThat(fileId).isNotNull();
        // Проверка в MinIO
    }
}
```

## Нагрузочное тестирование

### Сценарии тестирования

#### API Gateway нагрузочный тест
```java
@Test
void testConcurrentRequests() {
    int threads = 50;
    int requestsPerThread = 20;
    
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    CountDownLatch latch = new CountDownLatch(threads);
    
    for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
            try {
                for (int j = 0; j < requestsPerThread; j++) {
                    restTemplate.getForEntity("/api/quests", String.class);
                    recordSuccess();
                }
            } catch (Exception e) {
                recordFailure();
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await(5, TimeUnit.MINUTES);
    
    assertThat(getSuccessRate()).isGreaterThan(95.0);
}
```

#### Kafka нагрузочный тест
```java
@Test
void testHighVolumeEventProduction() {
    int eventsCount = 1000;
    int threads = 20;
    
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    
    for (int i = 0; i < threads; i++) {
        final int threadId = i;
        executor.submit(() -> {
            for (int j = 0; j < eventsCount / threads; j++) {
                String event = createTestEvent("event-" + threadId + "-" + j);
                kafkaTemplate.send("test-topic", event);
                recordSuccess();
            }
        });
    }
    
    executor.shutdown();
    executor.awaitTermination(2, TimeUnit.MINUTES);
    
    assertThat(getSuccessRate()).isGreaterThan(99.0);
}
```

### Метрики производительности

- **Пропускная способность**: запросов в секунду
- **Время отклика**: среднее, минимальное, максимальное
- **Уровень ошибок**: процент неудачных запросов
- **Использование ресурсов**: CPU, память, сеть

## CI/CD интеграция

### GitHub Actions

```yaml
name: Test Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run unit tests
      run: ./gradlew test
    - name: Generate test report
      run: ./scripts/generate-test-report.sh --type=summary

  integration-tests:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run integration tests
      run: ./scripts/run-integration-tests.sh --no-cleanup

  e2e-tests:
    runs-on: ubuntu-latest
    needs: [unit-tests, integration-tests]
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run E2E tests
      run: ./scripts/run-all-tests.sh --skip-unit --skip-integration

  load-tests:
    runs-on: ubuntu-latest
    needs: [e2e-tests]
    if: github.ref == 'refs/heads/main'
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run load tests
      run: ./scripts/run-load-tests.sh --concurrent-users=100 --test-duration=300
```

### GitLab CI

```yaml
stages:
  - test
  - integration
  - e2e
  - load

unit_tests:
  stage: test
  image: openjdk:17-jdk
  script:
    - ./gradlew test
    - ./scripts/generate-test-report.sh --type=summary
  artifacts:
    reports:
      junit: build/test-results/test/TEST-*.xml
    paths:
      - test-reports/

integration_tests:
  stage: integration
  image: openjdk:17-jdk
  services:
    - postgres:14
    - redis:6-alpine
  script:
    - ./scripts/run-integration-tests.sh

e2e_tests:
  stage: e2e
  image: openjdk:17-jdk
  script:
    - ./scripts/run-all-tests.sh --skip-unit --skip-integration
  dependencies:
    - integration_tests

load_tests:
  stage: load
  image: openjdk:17-jdk
  script:
    - ./scripts/run-load-tests.sh --concurrent-users=50
  only:
    - main
```

## Отчеты о тестировании

### Генерация отчетов

```bash
# Комплексный отчет
./scripts/generate-test-report.sh

# JSON отчет
./scripts/generate-test-report.sh --format=json

# Отчет без графиков
./scripts/generate-test-report.sh --no-charts

# Отчет с трендами
./scripts/generate-test-report.sh --trends --days=30
```

### Структура отчета

HTML отчет включает:

- **Общая статистика**: количество тестов, успешность, время выполнения
- **Детализация по типам**: unit, интеграционные, E2E, нагрузочные
- **Метрики покрытия**: инструкции, ветвления, строки
- **Качество кода**: дублирование, технический долг
- **Графики и диаграммы**: визуализация результатов
- **Логи выполнения**: подробные логи тестов

### Автоматическая публикация

```bash
# Публикация в GitLab Pages
./scripts/publish-test-report.sh

# Отправка в Slack
./scripts/notify-test-results.sh --channel=#testing --webhook=$SLACK_WEBHOOK

# Email уведомление
./scripts/email-test-report.sh --to=team@example.com
```

## Лучшие практики

### 1. Организация тестов

- **Структура**: Следуйте единой структуре пакетов для тестов
- **Именование**: Используйте описательные имена для тестов
- **Документация**: Добавляйте документацию для сложных тестовых сценариев

### 2. Тестовые данные

- **Изоляция**: Каждый тест должен быть независимым
- **Очистка**: Всегда очищайте тестовые данные после выполнения
- **Фабрики**: Используйте фабрики для создания тестовых данных

### 3. Ассерты

- **Конкретные сообщения**: Используйте описательные сообщения в ассертах
- **Группировка**: Группируйте связанные ассерты
- **Важные проверки**: Проверяйте только важные аспекты

### 4. Мокирование

- **Только внешние зависимости**: Мокируйте только внешние зависимости
- **Реалистичное поведение**: Убедитесь, что моки ведут себя реалистично
- **Верификация**: Верифицируйте вызовы моков

### 5. Производительность тестов

- **Быстрые тесты**: Держите unit тесты быстрыми
- **Параллельное выполнение**: Используйте параллельное выполнение где возможно
- **Оптимизация**: Оптимизируйте медленные тесты

### 6. Непрерывная интеграция

- **Быстрая обратная связь**: Получайте быструю обратную связь от тестов
- **Стабильность**: Убедитесь, что тесты стабильны и надежны
- **Покрытие**: Отслеживайте покрытие кода тестами

### 7. Мониторинг

- **Метрики**: Собирайте метрики выполнения тестов
- **Тренды**: Отслеживайте тренды производительности тестов
- **Оповещения**: Настройте оповещения о сбоях тестов

## Примеры тестовых сценариев

### Сценарий 1: Регистрация и аутентификация

```java
@Test
void testUserRegistrationAndAuthentication() {
    // Регистрация
    RegisterRequest registerRequest = TestDataFactory.createRegisterRequest();
    ResponseEntity<UserResponse> registerResponse = restTemplate.postForEntity(
        "/api/auth/register", registerRequest, UserResponse.class);
    
    assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    
    // Аутентификация
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername(registerRequest.getUsername());
    loginRequest.setPassword(registerRequest.getPassword());
    
    ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
        "/api/auth/login", loginRequest, AuthResponse.class);
    
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody().getToken()).isNotEmpty();
}
```

### Сценарий 2: Создание и выполнение квеста

```java
@Test
void testQuestCreationAndCompletion() {
    // Создание квеста
    CreateQuestRequest questRequest = TestDataFactory.createCreateQuestRequest();
    ResponseEntity<QuestResponse> questResponse = restTemplate.postForEntity(
        "/api/quests", questRequest, QuestResponse.class);
    
    assertThat(questResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID questId = questResponse.getBody().getId();
    
    // Публикация квеста
    restTemplate.put("/api/quests/" + questId + "/publish", null);
    
    // Начало игровой сессии
    CreateSessionRequest sessionRequest = new CreateSessionRequest();
    sessionRequest.setQuestId(questId);
    
    ResponseEntity<GameSessionResponse> sessionResponse = restTemplate.postForEntity(
        "/api/game-sessions", sessionRequest, GameSessionResponse.class);
    
    assertThat(sessionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID sessionId = sessionResponse.getBody().getId();
    
    // Выполнение задания
    CodeSubmissionRequest submission = new CodeSubmissionRequest();
    submission.setCode("print('Hello, World!')");
    submission.setLanguage("python");
    
    ResponseEntity<SubmissionResponse> submissionResponse = restTemplate.postForEntity(
        "/api/game-sessions/" + sessionId + "/submit", submission, SubmissionResponse.class);
    
    assertThat(submissionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(submissionResponse.getBody().isCorrect()).isTrue();
    
    // Завершение сессии
    restTemplate.postForEntity("/api/game-sessions/" + sessionId + "/complete", null, Void.class);
    
    // Проверка статистики
    ResponseEntity<UserStatisticsResponse> statsResponse = restTemplate.getForEntity(
        "/api/statistics/user/" + userId, UserStatisticsResponse.class);
    
    assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(statsResponse.getBody().getCompletedQuests()).isEqualTo(1);
}
```

### Сценарий 3: Командная игра

```java
@Test
void testTeamGameplay() {
    // Создание команды
    CreateTeamRequest teamRequest = TestDataFactory.createCreateTeamRequest();
    ResponseEntity<TeamResponse> teamResponse = restTemplate.postForEntity(
        "/api/teams", teamRequest, TeamResponse.class);
    
    UUID teamId = teamResponse.getBody().getId();
    
    // Генерация приглашения
    ResponseEntity<InvitationResponse> invitationResponse = restTemplate.postForEntity(
        "/api/teams/" + teamId + "/invitations", null, InvitationResponse.class);
    
    String inviteCode = invitationResponse.getBody().getInviteCode();
    
    // Присоединение второго игрока
    JoinTeamRequest joinRequest = new JoinTeamRequest();
    joinRequest.setInviteCode(inviteCode);
    
    restTemplate.postForEntity("/api/teams/" + teamId + "/join", joinRequest, Void.class);
    
    // Создание командной игровой сессии
    CreateSessionRequest sessionRequest = new CreateSessionRequest();
    sessionRequest.setQuestId(questId);
    sessionRequest.setTeamId(teamId);
    
    ResponseEntity<GameSessionResponse> sessionResponse = restTemplate.postForEntity(
        "/api/game-sessions", sessionRequest, GameSessionResponse.class);
    
    // Проверка командной статистики
    ResponseEntity<TeamStatisticsResponse> teamStatsResponse = restTemplate.getForEntity(
        "/api/statistics/team/" + teamId, TeamStatisticsResponse.class);
    
    assertThat(teamStatsResponse.getBody().getTotalMembers()).isEqualTo(2);
}
```

## Заключение

Это руководство охватывает все аспекты тестирования в DN Quest. Следуя этим практикам и используя предоставленные инструменты, вы можете обеспечить высокое качество и надежность микросервисной архитектуры.

Для получения дополнительной информации или помощи, обратитесь к команде разработки или создайте issue в репозитории проекта.