# Микросервисная архитектура DN Quest

## Обзор

Текущее монолитное приложение DN Quest планируется разделить на микросервисы для улучшения масштабируемости, отказоустойчивости и независимой разработки компонентов.

## Предлагаемая архитектура

### 1. API Gateway (Шлюз API)
**Ответственности:**
- Маршрутизация запросов к соответствующим микросервисам
- Аутентификация и авторизация
- Rate limiting
- Логирование запросов
- Агрегация ответов

**Технологии:**
- Spring Cloud Gateway
- Spring Security
- Redis для rate limiting

### 2. Authentication Service (Сервис аутентификации)
**Ответственности:**
- Регистрация и аутентификация пользователей
- Управление JWT токенами
- Восстановление пароля
- Управление ролями и правами

**База данных:** PostgreSQL (users, roles, permissions)

**API эндпоинты:**
- `/api/auth/register`
- `/api/auth/login`
- `/api/auth/refresh`
- `/api/auth/logout`
- `/api/auth/profile`

### 3. User Management Service (Сервис управления пользователями)
**Ответственности:**
- Управление профилями пользователей
- Поиск пользователей
- Управление аватарами
- Статистика пользователей

**База данных:** PostgreSQL (users, user_profiles)

**API эндпоинты:**
- `/api/users/{id}`
- `/api/users/search`
- `/api/users/{id}/profile`
- `/api/users/{id}/avatar`

### 4. Quest Management Service (Сервис управления квестами)
**Ответственности:**
- CRUD операции с квестами
- Управление уровнями
- Управление кодами и подсказками
- Валидация квестов
- Публикация квестов

**База данных:** PostgreSQL (quests, levels, codes, hints)

**API эндпоинты:**
- `/api/quests`
- `/api/quests/{id}`
- `/api/quests/{id}/levels`
- `/api/quests/{id}/publish`

### 5. Game Engine Service (Игровой движок)
**Ответственности:**
- Управление игровыми сессиями
- Обработка попыток ввода кодов
- Расчет времени и бонусов
- Автоматический переход между уровнями
- Валидация игровых правил

**База данных:** PostgreSQL (game_sessions, code_attempts, level_progress)

**API эндпоинты:**
- `/api/game/sessions`
- `/api/game/sessions/{id}/start`
- `/api/game/sessions/{id}/submit-code`
- `/api/game/sessions/{id}/current-level`

### 6. Team Management Service (Сервис управления командами)
**Ответственности:**
- Создание и управление командами
- Приглашения в команды
- Управление составом команды
- Передача прав капитана

**База данных:** PostgreSQL (teams, team_members, team_invitations)

**API эндпоинты:**
- `/api/teams`
- `/api/teams/{id}`
- `/api/teams/{id}/members`
- `/api/teams/{id}/invite`

### 7. Notification Service (Сервис уведомлений)
**Ответственности:**
- Email уведомления
- Push уведомления
- Telegram интеграция
- Управление шаблонами уведомлений

**База данных:** PostgreSQL (notifications, notification_templates)

**API эндпоинты:**
- `/api/notifications/send`
- `/api/notifications/templates`
- `/api/notifications/history`

### 8. Statistics Service (Сервис статистики)
**Ответственности:**
- Сбор игровой статистики
- Лидерборды
- Аналитика по квестам
- Отчеты для авторов

**База данных:** PostgreSQL + ClickHouse для аналитики

**API эндпоинты:**
- `/api/stats/leaderboard`
- `/api/stats/quests/{id}`
- `/api/stats/users/{id}`

### 9. File Storage Service (Сервис хранения файлов)
**Ответственности:**
- Хранение аватаров
- Хранение изображений квестов
- Хранение файлов уровней
- CDN интеграция

**Технологии:**
- MinIO или AWS S3
- CDN (CloudFlare)

**API эндпоинты:**
- `/api/files/upload`
- `/api/files/{id}`
- `/api/files/avatar/{userId}`

## Схема взаимодействия

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │  Mobile App     │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          └──────────┬───────────┘
                     │
          ┌─────────────────┐
          │  API Gateway    │
          └─────────┬───────┘
                    │
    ┌───────────────┼───────────────┐
    │               │               │
┌───▼───┐    ┌─────▼─────┐    ┌───▼───┐
│ Auth  │    │   Quest   │    │ Game  │
│Service│    │  Service  │    │Engine │
└───────┘    └───────────┘    └───────┘
    │               │               │
    └───────────────┼───────────────┘
                    │
        ┌─────────────────┐
        │  Message Broker │
        │    (Kafka)      │
        └─────────────────┘
```

## План миграции

### Этап 1: Подготовка инфраструктуры
1. Настройка Kubernetes кластера
2. Настройка service mesh (Istio)
3. Настройка мониторинга (Prometheus + Grafana)
4. Настройка логирования (ELK Stack)

### Этап 2: Выделение сервисов
1. **Authentication Service** - самый независимый сервис
2. **User Management Service** - зависит от Auth Service
3. **Quest Management Service** - относительно независимый
4. **Team Management Service** - зависит от User Management

### Этап 3: Игровые сервисы
1. **Game Engine Service** - самый сложный, требует осторожного выделения
2. **Statistics Service** - можно выделить параллельно
3. **Notification Service** - относительно независимый

### Этап 4: Вспомогательные сервисы
1. **File Storage Service**
2. **API Gateway** - настраивается постепенно

## Технологический стек

### Общие технологии
- **Java 21** + **Spring Boot 3.x**
- **PostgreSQL** - основные базы данных
- **Redis** - кэширование и сессии
- **Kafka** - message broker
- **Docker** + **Kubernetes** - контейнеризация и оркестрация

### Инфраструктура
- **Istio** - service mesh
- **Prometheus** + **Grafana** - мониторинг
- **ELK Stack** - логирование
- **Jaeger** - трейсинг
- **Nginx** + **CloudFlare** - CDN и балансировка

### Базы данных
- **PostgreSQL** - транзакционные данные
- **ClickHouse** - аналитические данные
- **Redis** - кэш и сессии
- **MinIO/S3** - файловое хранилище

## Коммуникация между сервисами

### Синхронная коммуникация
- **REST API** - для основных операций
- **gRPC** - для высоконагруженных операций

### Асинхронная коммуникация
- **Kafka** - для событий и интеграции
- **Redis Pub/Sub** - для простых уведомлений

### События
```java
// Примеры событий
UserRegisteredEvent
UserUpdatedEvent
QuestCreatedEvent
QuestPublishedEvent
GameSessionStartedEvent
GameSessionFinishedEvent
CodeSubmittedEvent
LevelCompletedEvent
TeamCreatedEvent
TeamMemberAddedEvent
```

## Конфигурация

### Environment Variables
```bash
# Общие
SPRING_PROFILES_ACTIVE=prod
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
REDIS_HOST=redis
POSTGRES_HOST=postgres

# Сервисы
AUTH_SERVICE_URL=http://auth-service:8080
QUEST_SERVICE_URL=http://quest-service:8080
GAME_ENGINE_URL=http://game-engine:8080
```

### Config Maps (Kubernetes)
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: dn-quest-config
data:
  database.url: "jdbc:postgresql://postgres:5432/dnquest"
  kafka.bootstrap.servers: "kafka:9092"
  redis.host: "redis"
```

## Безопасность

### Межсервисная аутентификация
- **mTLS** - для коммуникации между сервисами
- **Service Tokens** - JWT токены для сервисов
- **OAuth 2.0** - для внешней аутентификации

### Авторизация
- **RBAC** - ролевая модель доступа
- **Service Mesh Policies** - политики Istio
- **Network Policies** - ограничения доступа в Kubernetes

## Мониторинг и наблюдаемость

### Метрики
- **Prometheus** - сбор метрик
- **Grafana** - визуализация
- **Custom Metrics** - бизнес-метрики

### Логирование
- **ELK Stack** - централизованное логирование
- **Structured Logging** - структурированные логи
- **Correlation IDs** - отслеживание запросов

### Трейсинг
- **Jaeger** - распределенный трейсинг
- **OpenTelemetry** - стандарт трейсинга
- **Custom Spans** - бизнес-операции

## Резервное копирование и восстановление

### Базы данных
- **Wal-G** - бэкапы PostgreSQL
- **Point-in-time recovery** - восстановление на момент времени
- **Cross-region replication** - репликация между регионами

### Файлы
- **S3 Glacier** - долгосрочное хранение
- **CDN Caching** - кэширование статических файлов
- **Multi-region replication** - репликация файлов

## Производительность

### Оптимизация
- **Connection Pooling** - пулы соединений
- **Caching Strategy** - многоуровневое кэширование
- **Database Indexing** - оптимизация запросов
- **Async Processing** - асинхронная обработка

### Масштабирование
- **Horizontal Scaling** - масштабирование сервисов
- **Auto-scaling** - автоматическое масштабирование
- **Load Balancing** - балансировка нагрузки
- **Circuit Breakers** - защита от каскадных отказов

## Следующие шаги

1. **Создать PoC** для одного сервиса (Authentication Service)
2. **Настроить CI/CD** для микросервисов
3. **Реализовать API Gateway** с базовой маршрутизацией
4. **Настроить Kafka** для асинхронной коммуникации
5. **Мигрировать пользователей** в новый сервис
6. **Постепенно выделять** другие сервисы
7. **Настроить мониторинг** и наблюдаемость
8. **Провести нагрузочное тестирование**