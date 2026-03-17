# Рекомендации по улучшению архитектуры dn-quest

## Текущее состояние

Проект использует микросервисную архитектуру с 9 сервисами:
- `api-gateway` (8080)
- `authentication-service` (8081)
- `user-management-service` (8082)
- `quest-management-service` (8083)
- `game-engine-service` (8084)
- `team-management-service` (8085)
- `notification-service` (8086)
- `statistics-service` (8087)
- `file-storage-service` (8088)

---

## 🔴 ПРИОРИТЕТ 1: Для запуска (критично)

### 1. Консолидация PostgreSQL баз данных

**Проблема:** 8 отдельных экземпляров PostgreSQL избыточны для проекта такого масштаба.

**Решение:** Объединить в 2-3 базы данных:

```yaml
# Рекомендуемая схема:
# postgres-core: auth + users (связанные данные)
# postgres-business: quests + game + teams + notifications  
# postgres-analytics: statistics + files
```

**Пример конфигурации docker-compose:**
```yaml
postgres-core:
  environment:
    POSTGRES_MULTIPLE_DATABASES: dnquest_auth,dnquest_users

postgres-business:
  environment:
    POSTGRES_MULTIPLE_DATABASES: dnquest_quests,dnquest_game,dnquest_teams,dnquest_notifications

postgres-analytics:
  environment:
    POSTGRES_MULTIPLE_DATABASES: dnquest_statistics,dnquest_files
```

---

### 2. Исправление JWT Secret

**Проблема:** Hardcoded значения в docker-compose.yml и application.yml.

**Решение:** Вынести в переменные окружения.

```yaml
# docker-compose.yml
environment:
  JWT_SECRET: ${JWT_SECRET}  # Обязательная переменная без default
```

```properties
# application.yml
jwt:
  secret: ${JWT_SECRET}  # Без fallback для prod
```

---

### 3. Унификация health check путей

**Проблема:** Несоответствие путей:
- `authentication-service`: `/api/auth/actuator/health`
- `user-management-service`: `/api/users/actuator/health`
- Другие: `/actuator/health`

**Решение:** Унифицировать на `/actuator/health` через `server.servlet.context-path`.

```yaml
# docker-compose.yml
environment:
  SERVER_SERVLET_CONTEXT_PATH: /api/auth
```

---

## 🟠 ПРИОРИТЕТ 2: Для продакшена

### 4. Внедрение Service Discovery

**Проблема:** Hardcoded URL сервисов в конфигурации.

**Решение:** Добавить Consul или Eureka.

```yaml
# docker-compose.yml
consul:
  image: consul:1.15
  ports:
    - "8500:8500"
```

```yaml
# Конфигурация сервиса
spring:
  cloud:
    consul:
      host: consul
      port: 8500
      discovery:
        service-name: ${spring.application.name}
```

---

### 5. Внедрение mTLS

**Проблема:** Нет шифрования между сервисами.

**Решение:** Использовать Istio или Linkerd для service mesh.

```yaml
# Минимальная настройка mTLS через nginx
nginx:
  volumes:
    - ./ssl:/etc/nginx/ssl:ro
  environment:
    - SSL_CERT=/etc/nginx/ssl/cert.pem
    - SSL_KEY=/etc/nginx/ssl/key.pem
```

---

### 6. Переход на асинхронную коммуникацию

**Проблема:** Синхронные HTTP вызовы между сервисами.

**Решение:** Увеличить использование Kafka (уже настроен).

```java
// Пример: отправка события вместо синхронного вызова
@Service
@RequiredArgsConstructor
public class GameEventService {
    
    private final KafkaTemplate<String, GameEvent> kafkaTemplate;
    
    public void onLevelCompleted(LevelCompletedEvent event) {
        kafkaTemplate.send("game-events", event.getSessionId(), event);
    }
}
```

---

## 🟢 ПРИОРИТЕТ 3: Оптимизация

### 7. Центральный Config Server

**Решение:** Добавить Spring Cloud Config Server.

```yaml
config-server:
  image: dn-quest/config-server:latest
  environment:
    SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS: file:./config
```

---

### 8. ClickHouse для статистики

**Проблема:** PostgreSQL не оптимизирован для аналитических запросов.

**Решение:** Добавить ClickHouse для statistics-service.

```yaml
clickhouse:
  image: clickhouse/clickhouse-server:23.8
  ports:
    - "8123:8123"
  volumes:
    - clickhouse_data:/var/lib/clickhouse
```

---

## 📋 Резюме приоритетов

| Приоритет | Задача | Сложность | Влияние |
|----------|--------|-----------|---------|
| 1 | Консолидация PostgreSQL | Средняя | Высокое |
| 1 | Исправление JWT | Низкая | Критичное |
| 1 | Health check пути | Низкая | Среднее |
| 2 | Service Discovery | Высокая | Высокое |
| 2 | mTLS | Высокая | Высокое |
| 2 | Async Kafka | Средняя | Высокое |
| 3 | Config Server | Средняя | Среднее |
| 3 | ClickHouse | Средняя | Среднее |

---

## Следующие шаги

1. **Немедленно:** Исправить JWT secret и health check
2. **Перед запуском:** Консолидировать PostgreSQL
3. **После стабилизации:** Добавить Service Discovery
4. **Перед продакшеном:** Внедрить mTLS
