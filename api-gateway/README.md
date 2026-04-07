# DN Quest API Gateway

API Gateway для микросервисной архитектуры DN Quest, реализованный с использованием Spring Cloud Gateway.

## Обзор

API Gateway предоставляет единую точку входа для всех клиентских запросов к микросервисам DN Quest. Он обеспечивает маршрутизацию, аутентификацию, безопасность, мониторинг и отказоустойчивость.

## Основные функции

### 1. Аутентификация и авторизация
- JWT валидация для защищенных маршрутов
- Интеграция с Authentication Service для проверки токенов
- Автоматическое добавление пользовательских данных в заголовки

### 2. Circuit Breakers
- Защита от каскадных отказов с использованием Resilience4j
- Fallback механизмы для недоступных сервисов
- Retry логика с экспоненциальным backoff

### 3. Rate Limiting
- Ограничение запросов по IP и пользователям
- Персональные лимиты для разных эндпоинтов
- Использование Redis для распределенного ограничения

### 4. Логирование и мониторинг
- Логирование всех запросов/ответов
- Correlation ID для трейсинга запросов
- Метрики производительности с Prometheus
- Интеграция с Spring Boot Actuator

### 5. Безопасность
- CORS настройки
- Security headers (XSS protection, CSP, HSTS)
- Защита от common атак

## Архитектура

### Микросервисы
- **Authentication Service** (8081) - Аутентификация и управление пользователями
- **User Management Service** (8082) - Управление профилями пользователей
- **Quest Management Service** (8083) - Управление квестами
- **Game Engine Service** (8084) - Игровой движок
- **Team Management Service** (8085) - Управление командами
- **Notification Service** (8086) - Уведомления
- **Statistics Service** (8087) - Статистика и аналитика
- **File Storage Service** (8088) - Хранение файлов

### Маршрутизация
```
/api/auth/** -> Authentication Service
/api/users/** -> User Management Service
/api/quests/** -> Quest Management Service
/api/game/** -> Game Engine Service
/api/teams/** -> Team Management Service
/api/notifications/** -> Notification Service
/api/statistics/** -> Statistics Service
/api/files/** -> File Storage Service
```

## Конфигурация

### Переменные окружения
```bash
# JWT Configuration
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Service URLs
AUTHENTICATION_SERVICE_URL=http://localhost:8081
USER_MANAGEMENT_SERVICE_URL=http://localhost:8082
QUEST_MANAGEMENT_SERVICE_URL=http://localhost:8083
GAME_ENGINE_SERVICE_URL=http://localhost:8084
TEAM_MANAGEMENT_SERVICE_URL=http://localhost:8085
NOTIFICATION_SERVICE_URL=http://localhost:8086
STATISTICS_SERVICE_URL=http://localhost:8087
FILE_STORAGE_SERVICE_URL=http://localhost:8088
```

### Rate Limiting
Конфигурация лимитов запросов для разных сервисов:
- Authentication: 20 запросов/сек, burst 40
- User Management: 15 запросов/сек, burst 30
- Quest Management: 25 запросов/сек, burst 50
- Game Engine: 50 запросов/сек, burst 100
- Team Management: 10 запросов/сек, burst 20
- Notification: 100 запросов/сек, burst 200
- Statistics: 5 запросов/сек, burst 10
- File Storage: 20 запросов/сек, burst 40

## Эндпоинты мониторинга

### Health Check
- `GET /actuator/health` - Общее состояние системы
- `GET /actuator/gateway-health` - Детальная информация о состоянии Gateway
- `GET /actuator/circuit-breakers` - Состояние Circuit Breaker
- `GET /actuator/gateway-metrics` - Метрики производительности

### Метрики
- `GET /actuator/metrics` - Все метрики
- `GET /actuator/prometheus` - Метрики в формате Prometheus
- `GET /actuator/gateway` - Информация о маршрутах

## Безопасность

### JWT Токены
- Используются Bearer токены
- Токены валидируются локально и через Authentication Service
- Извлеченная информация добавляется в заголовки запроса

### Security Headers
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `X-XSS-Protection: 1; mode=block`
- `Content-Security-Policy`
- `Strict-Transport-Security` (для HTTPS)

### CORS
Разрешенные методы: GET, POST, PUT, DELETE, OPTIONS
Разрешенные заголовки: все
Поддержка credentials: включена

## Fallback механизмы

При недоступности микросервисов API Gateway возвращает стандартизированные ответы:
```json
{
  "error": "Service Unavailable",
  "message": "Сервис временно недоступен. Пожалуйста, попробуйте позже.",
  "service": "service-name",
  "timestamp": 1234567890
}
```

## Логирование

### Структура логов
```
Request started - CorrelationId: abc123, Method: GET, Path: /api/users/1, ClientIP: 192.168.1.1
Request completed - CorrelationId: abc123, StatusCode: 200, Duration: 150ms
```

### Уровни логирования
- `INFO` - Общая информация о запросах
- `WARN` - Медленные запросы (>1с), ошибки аутентификации
- `ERROR` - Ошибки сервера, срабатывания Circuit Breaker

## Разработка

### Запуск
```bash
./gradlew bootRun
```

### Тестирование
```bash
./gradlew test
```

### Профили
- `dev` - Разработка с локальными сервисами
- `prod` - Продакшн с service discovery
- `test` - Тестирование с mock сервисами

## Производительность

### Оптимизации
- Connection pooling для WebClient
- Компрессия ответов
- Кэширование статических ресурсов
- Оптимальные таймауты для разных сервисов

### Метрики
- Время обработки запросов
- Количество запросов в секунду
- Процент ошибок
- Состояние Circuit Breaker

## Траблшутинг

### Частые проблемы
1. **Circuit Breaker открыт** - Проверьте доступность микросервисов
2. **Rate Limiting** - Увеличьте лимиты или проверьте IP адреса
3. **JWT ошибки** - Проверьте секретный ключ и время жизни токенов

### Логи для диагностики
- `dn.quest.gateway` - Основные логи Gateway
- `org.springframework.cloud.gateway` - Логи Spring Cloud Gateway
- `io.github.resilience4j` - Логи Circuit Breaker

## Версии
- Spring Boot: 3.2.0
- Spring Cloud: 2023.0.0
- Resilience4j: 2.1.0
- Java: 21