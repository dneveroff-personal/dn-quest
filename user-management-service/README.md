# User Management Service

Микросервис для управления профилями, настройками и статистикой пользователей в платформе DN Quest.

## Обзор

User Management Service отвечает за:
- Управление профилями пользователей
- Хранение настроек приватности и уведомлений
- Ведение статистики активности и достижений
- Интеграцию с Authentication Service через Kafka
- Предоставление REST API для работы с пользовательскими данными

## Архитектура

### Основные компоненты

- **Entity**: UserProfile, UserSettings, UserStatistics
- **Repository**: JPA репозитории для работы с базой данных
- **Service**: Бизнес-логика управления пользователями
- **Controller**: REST API эндпоинты
- **Event**: Kafka события для синхронизации с другими сервисами

### База данных

Сервис использует PostgreSQL базу данных `dnquest_users` со следующими таблицами:
- `user_profiles` - профили пользователей
- `user_settings` - настройки пользователей
- `user_statistics` - статистика пользователей

## API Эндпоинты

### Управление профилями

#### Получение профиля
```http
GET /api/users/{id}
GET /api/users/profile/{userId}
GET /api/users/username/{username}
GET /api/users/email/{email}
```

#### Обновление профиля
```http
PUT /api/users/profile/{userId}
Content-Type: application/json

{
  "publicName": "Новое имя",
  "email": "new@example.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "bio": "Новая биография",
  "location": "Новое местоположение",
  "website": "https://example.com"
}
```

#### Управление аватаром
```http
PUT /api/users/{userId}/avatar?avatarUrl=https://example.com/avatar.jpg
DELETE /api/users/{userId}/avatar
```

#### Блокировка пользователей
```http
POST /api/users/{userId}/block
Content-Type: application/json

{
  "reason": "Причина блокировки",
  "permanent": false,
  "blockedUntil": "2024-12-31T23:59:59Z"
}

POST /api/users/{userId}/unblock
```

#### Активация
```http
POST /api/users/{userId}/activate
POST /api/users/{userId}/deactivate
```

#### Поиск пользователей
```http
GET /api/users?username=test&role=PLAYER&page=0&size=20&sortBy=createdAt&sortDirection=desc
```

### Управление настройками

#### Получение настроек
```http
GET /api/users/settings/{userId}
```

#### Обновление настроек
```http
PUT /api/users/settings/{userId}
Content-Type: application/json

{
  "profilePublic": true,
  "showEmail": false,
  "emailNotifications": true,
  "theme": "dark",
  "language": "ru",
  "autoJoinTeams": false
}
```

#### Частичное обновление настроек
```http
PUT /api/users/settings/{userId}/privacy?profilePublic=true&showEmail=false
PUT /api/users/settings/{userId}/notifications?emailNotifications=true
PUT /api/users/settings/{userId}/interface?theme=dark&language=ru
PUT /api/users/settings/{userId}/game?showHints=true&soundEffects=true
```

### Управление статистикой

#### Получение статистики
```http
GET /api/users/statistics/{userId}
```

#### Обновление статистики
```http
POST /api/users/statistics/{userId}/experience?experience=100
POST /api/users/statistics/{userId}/score?score=50
POST /api/users/statistics/{userId}/quest?completed=true&playtimeMinutes=30
POST /api/users/statistics/{userId}/login
```

#### Лидерборды
```http
GET /api/users/statistics/leaderboard/score?page=0&size=20
GET /api/users/statistics/leaderboard/level?page=0&size=20
GET /api/users/statistics/leaderboard/quests?page=0&size=20
GET /api/users/statistics/leaderboard/codes?page=0&size=20
GET /api/users/statistics/leaderboard/achievements?page=0&size=20
```

## Kafka События

### Публикуемые события

- **UserProfileCreatedEvent** - создание профиля пользователя
- **UserProfileUpdatedEvent** - обновление профиля
- **UserBlockedEvent** - блокировка пользователя
- **UserUnblockedEvent** - разблокировка пользователя
- **UserAvatarUpdatedEvent** - обновление аватара
- **UserActivityEvent** - активность пользователя

### Consumируемые события

- **UserRegisteredEvent** - регистрация пользователя (из Authentication Service)
- **UserUpdatedEvent** - обновление пользователя (из Authentication Service)
- **UserDeletedEvent** - удаление пользователя (из Authentication Service)
- **UserRoleChangedEvent** - изменение роли (из Authentication Service)
- **UserLoggedInEvent** - вход пользователя (из Authentication Service)

## Развертывание

### Локальная разработка

```bash
# Сборка проекта
./gradlew build

# Запуск с Docker Compose
docker compose up user-management-service

# Или локальный запуск
./gradlew :user-management-service:bootRun
```

### Переменные окружения

```bash
# База данных
DATABASE_URL=jdbc:postgresql://localhost:5432/dnquest_users
DATABASE_USERNAME=dn
DATABASE_PASSWORD=dn

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_USER_PROFILE_EVENTS_TOPIC=dn-quest.user-profile.events
KAFKA_AUTH_EVENTS_TOPIC=dn-quest.auth.events

# Authentication Service
AUTH_SERVICE_URL=http://localhost:8081

# Профиль
SPRING_PROFILES_ACTIVE=dev
```

### Docker

```bash
# Сборка образа
docker build -t dn-quest/user-management-service:latest .

# Запуск
docker run -p 8082:8082 \
  -e DATABASE_URL=jdbc:postgresql://postgres-users:5432/dnquest_users \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  dn-quest/user-management-service:latest
```

## Тестирование

### Запуск тестов

```bash
# Все тесты
./gradlew :user-management-service:test

# Только интеграционные тесты
./gradlew :user-management-service:integrationTest

# Тесты с покрытием
./gradlew :user-management-service:jacocoTestReport
```

### Тестовые данные

Для тестов используется H2 in-memory база данных и Testcontainers для интеграционных тестов.

## Мониторинг

### Health Check

```http
GET /api/users/actuator/health
```

### Метрики

```http
GET /api/users/actuator/metrics
GET /api/users/actuator/prometheus
```

### Логирование

Уровни логирования:
- `dn.quest.usermanagement`: INFO/DEBUG
- `org.springframework.security`: DEBUG (только для dev)
- `org.springframework.web`: DEBUG (только для dev)

## Безопасность

### Аутентификация

Сервис использует JWT токены для аутентификации. Токены валидируются через Authentication Service.

### Авторизация

- `ADMIN` - полный доступ ко всем эндпоинтам
- `USER` - доступ только к своим данным
- Публичные эндпоинты доступны без аутентификации

### Права доступа

```java
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
```

## Производительность

### Кэширование

- Настройки пользователей кэшируются в Caffeine
- Время жизни кэша: 30 минут
- Максимальный размер: 1000 записей

### Оптимизации базы данных

- Индексы на часто используемых полях
- Оптимизированные запросы с JOIN FETCH
- Пагинация для больших списков

## Интеграция

### Authentication Service

- Feign клиент для валидации токенов
- Kafka события для синхронизации данных
- Обновление профилей при изменении данных в Authentication Service

### File Storage Service

- Интеграция для управления аватарами
- Валидация URL аватаров
- Удаление аватаров при удалении профиля

### Statistics Service

- Экспорт статистики для аналитики
- Агрегированные данные для лидербордов
- События для обновления глобальной статистики

## Траблшутинг

### Частые проблемы

1. **Профиль не создается при регистрации**
   - Проверить Kafka подключение
   - Проверить обработку событий UserRegisteredEvent

2. **Медленные запросы**
   - Проверить индексы в базе данных
   - Проверить кэширование

3. **Ошибки авторизации**
   - Проверить валидацию JWT токенов
   - Проверить связь с Authentication Service

### Логи

```bash
# Просмотр логов контейнера
docker logs dn-quest-user-service

# Просмотр логов с фильтром
docker logs dn-quest-user-service | grep ERROR
```

## Версионирование

- Версия API: v1
- Формат версионирования: Semantic Versioning (semver)
- Обратная совместимость поддерживается в рамках мажорной версии

## Контакты

- Разработчик: DN Quest Team
- Документация: [Wiki](https://github.com/dn-quest/user-management-service/wiki)
- Issues: [GitHub Issues](https://github.com/dn-quest/user-management-service/issues)