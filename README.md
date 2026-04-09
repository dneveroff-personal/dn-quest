# DN Quest - Платформа для онлайн-квестов

[![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Makefile](https://img.shields.io/badge/Makefile-2C3E50)](https://www.gnu.org/software/make/manual/make.html)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-black?logo=apachekafka)](https://kafka.apache.org/)

Микросервисная платформа для создания и прохождения **командных квестов** в реальном времени.

## 🚀 Быстрый старт

### Запуск на локальной машине (Development)

```bash
make dev-up          # Запустить dev-окружение
make status          # Проверить статус всех сервисов
make open-all        # Открыть все интерфейсы в браузере

Все точки взаимодействия





















































СервисURLУчётные данныеНазначениеFrontend (Vue)http://localhost:3000—Основной пользовательский интерфейсAPI Gatewayhttp://localhost:8080admin / adminЕдиная входная точка для всех APISwagger UIhttp://localhost:8080/swagger-ui.htmladmin / adminИнтерактивная документация и тестирование эндпоинтовKafka UIhttp://localhost:8089—Просмотр Kafka-топиков и событий в реальном времениMinIO Consolehttp://localhost:9001minioadmin / minioadminХранилище файлов (фото квестов, медиа и т.д.)Grafana (full mode)http://localhost:3001admin / adminМониторинг метрик и дашбордыJaeger UIhttp://localhost:16686—Распределённая трассировка запросов


## 📋 Основные команды

Основные команды Makefile
Bashmake help                    # Показать все доступные команды
make dev-up                  # Запуск dev-окружения
make dev-down                # Остановить все сервисы
make dev-restart             # Полный рестарт
make status                  # Статус контейнеров
make logs SERVICE=xxx        # Логи конкретного сервиса (например: api-gateway-dev)
make build                   # Пересобрать все Java-сервисы
make open-all                # Открыть все URL в браузере
make clean                   # Полная очистка (включая volumes)

## 🏗️ Архитектура

### Микросервисы

API Gateway — единственная дверь (Spring Cloud Gateway + JWT-аутентификация + rate limiting)
8 микросервисов общаются между собой только асинхронно через Kafka
Базы: отдельный PostgreSQL на каждый сервис + Redis + MinIO
Основные события: UserRegistered, QuestCompleted, TeamJoined, CodeScanned и др.

| Сервис | Порт | Описание |
|--------|------|----------|
| **API Gateway** | 8080 | Маршрутизация, аутентификация, rate limiting |
| **Authentication Service** | 8081 | Регистрация, вход, JWT токены |
| **User Management Service** | 8082 | Управление профилями пользователей |
| **Quest Management Service** | 8083 | Создание и управление квестами |
| **Game Engine Service** | 8084 | Игровая логика, обработка кодов |
| **Team Management Service** | 8085 | Управление командами |
| **Notification Service** | 8086 | Уведомления (email, Telegram) |
| **Statistics Service** | 8087 | Статистика и аналитика |
| **File Storage Service** | 8088 | Хранение файлов (MinIO/S3) |
| **Frontend** | 3000 | Vue.js приложение |


## 🗂️ Структура проекта

```
dn-quest/
├── api-gateway/              # API Gateway (Spring Cloud Gateway)
├── authentication-service/   # Сервис аутентификации
├── user-management-service/ # Управление пользователями
├── quest-management-service/# Управление квестами
├── game-engine-service/     # Игровой движок
├── team-management-service/ # Управление командами
├── notification-service/     # Уведомления
├── statistics-service/       # Статистика
├── file-storage-service/    # Хранение файлов
├── dn-quest-shared/        # Общая библиотека
├── frontend/                # Vue.js приложение
├── docker/                  # Docker конфигурации
├── docs/                   # Документация
├── dn-quest.sh             # Главный скрипт управления
└── docker-compose*.yml      # Docker Compose файлы
```

## 📚 Документация

- [Полное руководство по Docker](DOCKER_SETUP_GUIDE.md)
- [Архитектура микросервисов](README-MICROSERVICES.md)
- [Конфигурация Docker](docs/docker-configuration-guide.md)
- [Интеграция Kafka](docs/kafka-integration.md)
- [Мониторинг](docs/monitoring-guide.md)

## 🔒 Безопасность (Production)

1. **Измените все пароли** в `.env.production`
2. **Настройте HTTPS** (SSL сертификаты)
3. **Измените JWT_SECRET** на длинную случайную строку
4. **Настройте firewall** — откройте только необходимые порты
5. **Включите мониторинг** и алерты

---
## Структура зависимостей

postgres, redis, kafka, minio     ← инфраструктура
↓
authentication-service            ← уже healthy!
↓
user-management, file-storage     ← зависят от auth
↓
quest-management, team-management ← зависят от auth + user/file
↓
game-engine                       ← зависит от quest + team
↓
statistics, notification          ← зависят от game
↓
api-gateway, frontend             ← уже healthy!


## 📄 Лицензия

MIT License