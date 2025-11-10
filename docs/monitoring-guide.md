# Руководство по мониторингу DN Quest

## Обзор

Система мониторинга DN Quest обеспечивает полную наблюдаемость за микросервисной архитектурой, включая метрики, логирование, трейсинг и алертинг.

## Архитектура мониторинга

### Компоненты

- **Prometheus** - Сбор и хранение метрик
- **Grafana** - Визуализация метрик и дашборды
- **AlertManager** - Управление алертами и уведомлениями
- **ELK Stack** - Централизованное логирование
  - **Elasticsearch** - Хранение и поиск логов
  - **Logstash** - Обработка и парсинг логов
  - **Kibana** - Визуализация логов
- **Jaeger** - Распределенное трейсинг
- **Node Exporter** - Метрики системы
- **cAdvisor** - Метрики Docker контейнеров

### Схема

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Микросервисы  │───▶│   Prometheus    │───▶│     Grafana     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       ▼                       │
         │              ┌─────────────────┐              │
         │              │  AlertManager   │              │
         │              └─────────────────┘              │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Jaeger      │    │   Logstash      │    │  Node Exporter  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  Elasticsearch  │
                       └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │     Kibana      │
                       └─────────────────┘
```

## Быстрый старт

### 1. Развертывание мониторинга

```bash
# Запуск мониторинговой инфраструктуры
./scripts/deploy-monitoring.sh

# Проверка статуса
docker-compose -f docker-compose.monitoring.yml ps
```

### 2. Доступ к сервисам

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)
- **AlertManager**: http://localhost:9093
- **Kibana**: http://localhost:5601
- **Jaeger**: http://localhost:16686

### 3. Тестирование алертов

```bash
# Тест всех алертов
./scripts/test-alerts.sh all

# Тест конкретного типа алертов
./scripts/test-alerts.sh service
./scripts/test-alerts.sh cpu
./scripts/test-alerts.sh memory
```

## Метрики

### Типы метрик

#### Системные метрики
- **CPU**: Загрузка процессора
- **Memory**: Использование памяти
- **Disk**: Использование дискового пространства
- **Network**: Сетевая активность

#### JVM метрики
- **Heap Memory**: Использование кучи
- **GC**: Сборка мусора
- **Threads**: Потоки выполнения
- **Classes**: Загруженные классы

#### Бизнес-метрики
- **User Activity**: Активность пользователей
- **Game Sessions**: Игровые сессии
- **Quest Completions**: Завершенные квесты
- **API Performance**: Производительность API

#### Метрики инфраструктуры
- **Database**: Производительность БД
- **Kafka**: Очереди сообщений
- **Redis**: Кэш
- **Containers**: Docker контейнеры

### Настройка метрик в микросервисах

#### 1. Добавление зависимостей

```kotlin
// build.gradle.kts
implementation("io.micrometer:micrometer-registry-prometheus")
implementation("io.micrometer:micrometer-tracing-bridge-brave")
implementation("io.zipkin.reporter2:zipkin-reporter-brave")
```

#### 2. Конфигурация

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.95,0.99

tracing:
  sampling:
    probability: 1.0
  zipkin:
    endpoint: http://localhost:9411/api/v2/spans
```

#### 3. Кастомные метрики

```java
@Component
public class BusinessMetrics {
    private final Counter userRegistrations;
    private final Timer requestDuration;
    private final Gauge activeUsers;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.userRegistrations = Counter.builder("user_registrations_total")
            .description("Total number of user registrations")
            .register(meterRegistry);
            
        this.requestDuration = Timer.builder("http_request_duration")
            .description("HTTP request duration")
            .register(meterRegistry);
            
        this.activeUsers = Gauge.builder("active_users_count")
            .description("Number of active users")
            .register(meterRegistry, this, BusinessMetrics::getActiveUsersCount);
    }

    public void recordUserRegistration() {
        userRegistrations.increment();
    }

    public void recordRequestDuration(Duration duration) {
        requestDuration.record(duration);
    }

    private double getActiveUsersCount() {
        // Логика подсчета активных пользователей
        return userService.getActiveUsersCount();
    }
}
```

## Дашборды Grafana

### Системные дашборды

#### 1. Обзор системы (System Overview)
- Состояние всех сервисов
- Общая загрузка системы
- Ключевые метрики производительности

#### 2. Бизнес-метрики (Business Metrics)
- Активность пользователей
- Игровые сессии
- Завершенные квесты
- Доход и монетизация

#### 3. Безопасность (Security Metrics)
- Попытки входа
- Ошибки аутентификации
- Блокировки
- Аномальная активность

### Индивидуальные дашборды сервисов

Каждый микросервис имеет свой дашборд с:
- Метриками производительности
- Ошибками и исключениями
- Бизнес-метриками
- Зависимостями

### Создание кастомных дашбордов

1. Импорт шаблона:
   ```bash
   curl -X POST \
     -u admin:admin123 \
     -H "Content-Type: application/json" \
     -d @dashboard.json \
     http://localhost:3000/api/dashboards/db
   ```

2. Создание через UI:
   - Открыть Grafana
   - Dashboard → New Dashboard
   - Add Panel → Configure metrics

## Алертинг

### Типы алертов

#### Критичные (Critical)
- Недоступность сервиса
- Ошибки базы данных
- Высокая загрузка CPU (>90%)
- Нехватка памяти (>95%)

#### Предупреждения (Warning)
- Высокая загрузка CPU (>70%)
- Высокое использование памяти (>80%)
- Медленные запросы (>1s)
- Низкая активность пользователей

#### Информационные (Info)
- Новые регистрации
- Достижения
- Системные события

### Правила алертов

```yaml
# docker/prometheus/rules/enhanced-alerts.yml
groups:
  - name: dn-quest-alerts
    rules:
      - alert: ServiceDown
        expr: up == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Сервис {{ $labels.job }} недоступен"
          description: "Сервис {{ $labels.job }} на {{ $labels.instance }} не отвечает более 1 минуты"

      - alert: HighCPUUsage
        expr: 100 - (avg by(instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Высокая загрузка CPU на {{ $labels.instance }}"
          description: "Загрузка CPU составляет {{ $value }}%"
```

### Каналы уведомлений

#### Email
```yaml
# docker/alertmanager/alertmanager.yml
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alerts@dn-quest.com'

route:
  receiver: 'email-notifications'

receivers:
  - name: 'email-notifications'
    email_configs:
      - to: 'admin@dn-quest.com'
        subject: '[DN Quest] {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          Алерт: {{ .Annotations.summary }}
          Описание: {{ .Annotations.description }}
          Время: {{ .StartsAt }}
          {{ end }}
```

#### Slack
```yaml
receivers:
  - name: 'slack-notifications'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
        channel: '#alerts'
        title: 'DN Quest Alert'
        text: '{{ range .Alerts }}{{ .Annotations.summary }}{{ end }}'
```

#### Telegram
```yaml
receivers:
  - name: 'telegram-notifications'
    telegram_configs:
      - bot_token: 'YOUR_BOT_TOKEN'
        chat_id: YOUR_CHAT_ID
        message: '{{ range .Alerts }}{{ .Annotations.summary }}{{ end }}'
```

### Тестирование алертов

```bash
# Тест всех алертов
./scripts/test-alerts.sh all

# Тест конкретного алерта
./scripts/test-alerts.sh service

# Проверка статуса
./scripts/test-alerts.sh status

# Очистка тестовых алертов
./scripts/test-alerts.sh cleanup
```

## Логирование

### Структура логов

```json
{
  "@timestamp": "2024-01-15T10:30:00.000Z",
  "level": "INFO",
  "logger": "dn.quest.authentication.service.AuthService",
  "thread": "http-nio-8080-exec-1",
  "message": "User authenticated successfully",
  "application": "authentication-service",
  "environment": "production",
  "trace_id": "abc123",
  "span_id": "def456",
  "user_id": "user123",
  "request_id": "req789"
}
```

### Уровни логирования

- **ERROR**: Критические ошибки
- **WARN**: Предупреждения
- **INFO**: Информационные сообщения
- **DEBUG**: Отладочная информация

### Конфигурация логирования

```java
@Configuration
public class LoggingConfiguration {
    
    @Bean
    public Logger structuredLogger() {
        return LoggerFactory.getLogger("dn.quest.structured");
    }
    
    public void logUserAction(String userId, String action) {
        MDC.put("user_id", userId);
        MDC.put("action", action);
        
        structuredLogger.info("User action performed");
        
        MDC.clear();
    }
}
```

### Поиск в Kibana

1. Открыть Kibana: http://localhost:5601
2. Создать индексный паттерн: `dn-quest-logs-*`
3. Искать по полям:
   - `application: "authentication-service"`
   - `level: "ERROR"`
   - `user_id: "user123"`

## Трейсинг

### Конфигурация трейсинга

```java
@Configuration
public class TracingConfiguration {
    
    @Bean
    public TracingUtils tracingUtils(Tracer tracer) {
        return new TracingUtils(tracer);
    }
}

@Component
public class UserService {
    
    @Autowired
    private TracingUtils tracingUtils;
    
    public User createUser(UserDTO userDTO) {
        return tracingUtils.trace("createUser", () -> {
            // Логика создания пользователя
            return userRepository.save(user);
        });
    }
}
```

### Просмотр трейсов в Jaeger

1. Открыть Jaeger: http://localhost:16686
2. Выбрать сервис
3. Установить временной диапазон
4. Искать трейсы
5. Анализировать производительность

## Мониторинг инфраструктуры

### Docker контейнеры

```bash
# Просмотр метрик контейнеров
curl http://localhost:9323/metrics

# Мониторинг конкретного контейнера
docker stats <container_name>
```

### Системные метрики

```bash
# Просмотр метрик Node Exporter
curl http://localhost:9100/metrics

# Анализ загрузки CPU
curl -s 'http://localhost:9100/metrics' | grep node_cpu_seconds_total
```

### База данных

```sql
-- Активные соединения
SELECT count(*) FROM pg_stat_activity;

-- Медленные запросы
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

## Процедуры реагирования

### Критичные алерты

1. **Сервис недоступен**
   - Проверить статус контейнера: `docker ps`
   - Просмотреть логи: `docker logs <service>`
   - Перезапустить сервис: `docker restart <service>`
   - Проверить зависимости (БД, Kafka)

2. **Высокая загрузка CPU**
   - Идентифицировать процесс: `top`
   - Проверить количество запросов
   - Анализировать медленные запросы
   - Рассмотреть масштабирование

3. **Нехватка памяти**
   - Проверить использование памяти: `free -h`
   - Анализировать утечки памяти
   - Увеличить лимиты контейнера
   - Перезапустить сервис

### Предупреждения

1. **Медленные запросы**
   - Анализировать трейсы в Jaeger
   - Проверить индексы в БД
   - Оптимизировать запросы
   - Добавить кэширование

2. **Низкая активность пользователей**
   - Проверить маркетинговые кампании
   - Анализировать пользовательский опыт
   - Проверить работоспособность фич

## Обслуживание

### Резервное копирование

```bash
# Резервное копирование конфигураций
./scripts/backup-monitoring.sh

# Резервное копирование данных Prometheus
docker exec prometheus tar czf /tmp/prometheus-data.tar.gz /prometheus

# Резервное копирование Elasticsearch
curl -X PUT "localhost:9200/_snapshot/backup/snapshot_1"
```

### Обновление

```bash
# Обновление компонентов
docker-compose -f docker-compose.monitoring.yml pull

# Перезапуск с обновлением
docker-compose -f docker-compose.monitoring.yml up -d

# Проверка после обновления
./scripts/test-alerts.sh all
```

### Очистка

```bash
# Очистка старых логов
docker exec logstash /usr/share/logstash/bin/logstash --path.settings=/etc/logstash -f /etc/logstash/conf.d/cleanup.conf

# Очистка старых метрик
curl -X POST http://localhost:9090/api/v1/admin/tsdb/delete_series?match[]=__name__=".+"

# Очистка диска
docker system prune -f
```

## Best Practices

### Метрики
1. Используйте осмысленные имена метрик
2. Добавляйте описание и метки
3. Избегайте высококардинальных метрик
4. Используйте гистограммы для распределений

### Алерты
1. Устанавливайте пороги на основе реальных данных
2. Используйте ингибиции для предотвращения спама
3. Настройте эскалацию алертов
4. Регулярно тестируйте алерты

### Логирование
1. Используйте структурированные логи
2. Добавляйте контекст (trace_id, user_id)
3. Избегайте логирования чувствительных данных
4. Используйте соответствующие уровни логирования

### Трейсинг
1. Добавляйте бизнес-контекст в трейсы
2. Используйте семантические конвенции
3. Ограничивайте сэмплинг в production
4. Анализируйте критические пути

## Траблшутинг

### Частые проблемы

1. **Prometheus не собирает метрики**
   - Проверить доступность эндпоинта: `curl http://service:port/actuator/prometheus`
   - Проверить конфигурацию scrape_configs
   - Проверить сетевую доступность

2. **Алерты не отправляются**
   - Проверить конфигурацию AlertManager
   - Проверить правила алертов
   - Проверить настройки уведомлений

3. **Логи не появляются в Kibana**
   - Проверить конфигурацию Logstash
   - Проверить индексные паттерны
   - Проверить формат логов

4. **Трейсы не видны в Jaeger**
   - Проверить конфигурацию трейсинга
   - Проверить сэмплинг
   - Проверить экспортер

### Диагностические команды

```bash
# Проверка здоровья сервисов
curl http://localhost:9090/-/healthy
curl http://localhost:3000/api/health
curl http://localhost:9200/_cluster/health

# Просмотр логов
docker-compose -f docker-compose.monitoring.yml logs -f prometheus
docker-compose -f docker-compose.monitoring.yml logs -f alertmanager

# Проверка конфигураций
curl http://localhost:9090/api/v1/status/config
curl http://localhost:9093/api/v1/status
```

## Заключение

Система мониторинга DN Quest обеспечивает полную наблюдаемость за микросервисной архитектурой. Регулярное обслуживание и обновление компонентов гарантируют стабильную работу и быстрое выявление проблем.

Для дополнительной информации обращайтесь к:
- [Документации Prometheus](https://prometheus.io/docs/)
- [Документации Grafana](https://grafana.com/docs/)
- [Документации ELK Stack](https://www.elastic.co/guide/)
- [Документации Jaeger](https://www.jaegertracing.io/docs/)