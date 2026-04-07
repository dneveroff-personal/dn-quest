# DN Quest Microservices Architecture

Это микросервисная архитектура для DN Quest, созданная с использованием Gradle multi-module setup, Spring Boot 3.x, Java 21 и современных практик разработки.

## 🏗️ Архитектура

### Микросервисы

- **api-gateway** (порт 8080) - API Gateway для маршрутизации запросов
- **authentication-service** (порт 8081) - Сервис аутентификации и авторизации
- **user-management-service** (порт 8082) - Управление пользователями
- **quest-management-service** (порт 8083) - Управление квестами
- **game-engine-service** (порт 8084) - Игровой движок
- **team-management-service** (порт 8085) - Управление командами
- **notification-service** (порт 8086) - Сервис уведомлений
- **statistics-service** (порт 8087) - Сервис статистики
- **file-storage-service** (порт 8088) - Сервис хранения файлов

### Shared Library

**dn-quest-shared** - Общая библиотека с:
- Общими DTO классами
- Enums и константами
- Утилитарными классами (DateTimeUtils, DtoMapper и др.)
- Общими конфигурациями безопасности

### Инфраструктура

- **PostgreSQL** - Базы данных для каждого сервиса
- **Redis** - Кэширование и управление сессиями
- **Kafka** - Event-driven коммуникация
- **MinIO** - Объектное хранилище файлов

## 🚀 Быстрый старт

### Требования

- Java 21+
- Docker & Docker Compose
- Gradle 8.0+

### Сборка и запуск

1. **Сборка всех сервисов:**
   ```bash
   ./scripts/build.sh
   ```

2. **Запуск всех сервисов:**
   ```bash
   ./scripts/start.sh
   ```

3. **Проверка статуса:**
   ```bash
   ./scripts/status.sh
   ```

4. **Остановка сервисов:**
   ```bash
   ./scripts/stop.sh
   ```

### Дополнительные опции

Запуск с определенной средой:
```bash
./scripts/start.sh --env prod
```

Сборка и запуск:
```bash
./scripts/start.sh --build
```

Запуск только определенных сервисов:
```bash
./scripts/start.sh --services api-gateway,authentication-service
```

Полная очистка:
```bash
./scripts/stop.sh --all
```

## 📡 Доступ к сервисам

### API Endpoints

- **API Gateway**: http://localhost:8080
- **Authentication Service**: http://localhost:8081
- **User Management Service**: http://localhost:8082
- **Quest Management Service**: http://localhost:8083
- **Game Engine Service**: http://localhost:8084
- **Team Management Service**: http://localhost:8085
- **Notification Service**: http://localhost:8086
- **Statistics Service**: http://localhost:8087
- **File Storage Service**: http://localhost:8088

### Инфраструктура

- **Kafka UI**: http://localhost:8089
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)

### Документация

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 🏛️ Структура проекта

```
dn-quest/
├── settings.gradle.kts              # Конфигурация Gradle multi-module
├── build.gradle.kts                 # Корневая конфигурация сборки
├── docker-compose.yml               # Docker Compose для всех сервисов
├── scripts/                         # Скрипты управления
│   ├── build.sh                     # Сборка всех сервисов
│   ├── start.sh                     # Запуск сервисов
│   ├── stop.sh                      # Остановка сервисов
│   └── status.sh                    # Проверка статуса
├── dn-quest-shared/                 # Общая библиотека
├── api-gateway/                     # API Gateway
├── authentication-service/          # Сервис аутентификации
├── user-management-service/         # Управление пользователями
├── quest-management-service/        # Управление квестами
├── game-engine-service/             # Игровой движок
├── team-management-service/         # Управление командами
├── notification-service/            # Сервис уведомлений
├── statistics-service/              # Сервис статистики
└── file-storage-service/            # Хранение файлов
```

## 🔧 Разработка

### Локальная разработка

Для разработки отдельного сервиса:

```bash
# Запуск только инфраструктуры
docker compose up -d postgres-auth redis kafka

# Запуск сервиса локально
cd authentication-service
../gradlew bootRun
```

### Тестирование

```bash
# Запуск всех тестов
./gradlew test

# Тесты конкретного модуля
./gradlew :authentication-service:test
```

### Сборка Docker образов

```bash
# Сборка всех образов
./scripts/build.sh

# Сборка конкретного сервиса
docker build -t dn-quest/authentication-service:latest ./authentication-service
```

## 📊 Мониторинг

### Health Checks

Все сервисы имеют health checks по пути `/actuator/health`:

```bash
curl http://localhost:8080/actuator/health
```

### Метрики

Prometheus метрики доступны по пути `/actuator/prometheus`:

```bash
curl http://localhost:8080/actuator/prometheus
```

### Логи

Просмотр логов всех сервисов:
```bash
docker compose logs -f
```

Логи конкретного сервиса:
```bash
docker compose logs -f authentication-service
```

## 🔒 Безопасность

### JWT Аутентификация

- JWT токены генерируются в authentication-service
- Все сервисы проверяют токены через API Gateway
- Срок действия токена: 24 часа

### Межсервисная коммуникация

- Сервисы общаются через Kafka для асинхронных операций
- Синхронные запросы идут через API Gateway
- Используется Spring Cloud LoadBalancer для балансировки

## 🗄️ Базы данных

Каждый сервис имеет свою базу данных PostgreSQL:

- **auth**: dnquest_auth (порт 5432)
- **users**: dnquest_users (порт 5433)
- **quests**: dnquest_quests (порт 5434)
- **game**: dnquest_game (порт 5435)
- **teams**: dnquest_teams (порт 5436)
- **notifications**: dnquest_notifications (порт 5437)
- **statistics**: dnquest_statistics (порт 5438)
- **files**: dnquest_files (порт 5439)

### Миграции

Используется Flyway для миграций баз данных. Миграции находятся в `src/main/resources/db/migration` каждого сервиса.

## 📨 Event-Driven Architecture

### Kafka Topics

- `user-events` - События пользователей
- `quest-events` - События квестов
- `game-events` - Игровые события
- `team-events` - События команд
- `notification-events` - События уведомлений

### Пример события

```json
{
  "eventId": "uuid",
  "eventType": "USER_CREATED",
  "timestamp": "2024-01-01T00:00:00Z",
  "payload": {
    "userId": "uuid",
    "email": "user@example.com"
  }
}
```

## 🐳 Docker

### Образы

Все сервисы имеют оптимизированные Docker образы:
- Базовый образ: `openjdk:21-jre-slim`
- Многопользовательский режим для безопасности
- Health checks для мониторинга
- Оптимизированные JVM настройки

### Переменные окружения

Основные переменные окружения:

```bash
# База данных
DATABASE_URL=jdbc:postgresql://host:port/database
DATABASE_USERNAME=username
DATABASE_PASSWORD=password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

## 🔧 Конфигурация

### Профили

- **dev** - Разработка (по умолчанию)
- **prod** - Продакшн
- **test** - Тестирование

### Конфигурационные файлы

- `application.yml` - Основная конфигурация
- `application-dev.yml` - Настройки для разработки
- `application-prod.yml` - Настройки для продакшн
- `application-test.yml` - Настройки для тестов

## 🚀 Развертывание

### Продакшн

Для развертывания в продакшн:

1. Настройте переменные окружения
2. Используйте SSL сертификаты
3. Настройте мониторинг и логирование
4. Используйте Kubernetes или Docker Swarm

```bash
# Продакшн запуск
./scripts/start.sh --env prod
```

### Kubernetes

Манифесты для Kubernetes могут быть сгенерированы из Docker Compose конфигурации.

## 🛠️ Устранение проблем

### Частые проблемы

1. **Порты уже заняты**
   ```bash
   # Проверка занятых портов
   netstat -tulpn | grep :8080
   # Освобождение портов
   ./scripts/stop.sh
   ```

2. **Проблемы с памятью**
   ```bash
   # Увеличение памяти Docker
   # В Docker Desktop: Settings > Resources > Memory
   ```

3. **Сервисы не стартуют**
   ```bash
   # Проверка логов
   docker compose logs service-name
   # Проверка зависимостей
   docker compose ps
   ```

### Полная переустановка

```bash
# Полная очистка и переустановка
./scripts/stop.sh --all
docker system prune -a
./scripts/build.sh
./scripts/start.sh
```

## 📚 Дополнительная документация

- [API Documentation](http://localhost:8080/swagger-ui.html)
- [Kafka UI](http://localhost:8089)
- [MinIO Console](http://localhost:9001)

## 🤝 Вклад в проект

1. Fork проекта
2. Создайте feature branch
3. Внесите изменения
4. Создайте Pull Request

## 📄 Лицензия

MIT License