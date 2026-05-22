# 🎮 DN Quest — Платформа для командных онлайн-квестов

[![Java 21](https://img.shields.io/badge/Java-21-007396?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.5-02303A?style=flat&logo=gradle&logoColor=white)](https://gradle.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=flat&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)

> Современная микросервисная платформа для создания и прохождения **командных квестов** в реальном времени.
> Построена на Java 21, Spring Boot 3.2 и архитектуре событийного взаимодействия.

---

## 📦 Содержание

- [Быстрый старт](#-быстрый-старт)
- [Архитектура](#-архитектура)
- [Микросервисы](#-микросервисы)
- [Технологический стек](#-технологический-стек)
- [Запуск](#-запуск)
- [Конфигурация](#-конфигурация)
- [Мониторинг](#-мониторинг)
- [Безопасность](#-безопасность)

---

## 🚀 Быстрый старт

### Предварительные требования

| Инструмент | Версия | Назначение |
|------------|--------|------------|
| Docker | 24.0+ | Контейнеризация |
| Docker Compose | 2.20+ | Оркестрация сервисов |
| Java | 21 | Среда выполнения |
| Gradle | 8.5 | Сборка проекта |
| Node.js | 18+ | Среда для frontend |

### Запуск за 5 минут

```bash
# 1. Клонирование репозитория
git clone https://github.com/your-repo/dn-quest.git
cd dn-quest

# 2. Сборка проекта (backend)
make build

# 3. Сборка frontend
cd frontend && npm install && npm run build

# 4. Запуск всех сервисов
make dev-up

# 5. Проверка статуса
make status
```

После запуска доступны:

| Сервис | URL | Учётные данные | Примечание |
|--------|-----|--------|------------|
| Frontend | http://localhost:3000 | — | Vue.js приложение |
| API Gateway | http://localhost:8080 | admin / admin | Основная точка входа |
| Swagger UI | http://localhost:8080/swagger-ui.html | admin / admin | API документация |
| Kafka UI | http://localhost:8089 | — | Управление топиками |
| pgAdmin | http://localhost:5432 | — | Администрирование PostgreSQL |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin | S3 хранилище |
| Jaeger | http://localhost:16686 | — | Трассировка |

---

## 🏗️ Архитектура

### Высокоуровневая схема

```
                            ┌─────────────────┐
                            │    Frontend     │
                            │   (Vue.js)      │
                            │   :3000         │
                            └────────┬────────┘
                                     │
                            ┌────────▼────────┐
                            │   API Gateway  │
                            │    :8080       │
                            └────────┬────────┘
                                     │
    ┌──────────────┬──────────────┬──┴────┬──────────────┬──────────────┐
    │              │              │       │              │              │
┌───▼───┐     ┌────▼────┐   ┌────▼────┐ ┌───▼────┐   ┌───▼────┐   ┌────▼────┐
│ Auth  │     │  User   │   │  Quest  │ │ Game   │   │ Team  │   │  File   │
│:8081  │     │ :8082   │   │ :8083   │ │ :8084  │   │:8085  │   │ :8088   │
└───┬───┘     └────┬────┘   └────┬────┘ └───┬────┘   └───┬────┘   └────┬────┘
    │              │              │         │         │              │
    └──────────────┴──────────────┴─────────┴─────────┴──────────────┘
                                     │
                            ┌────────▼────────┐
                            │    Kafka        │
                            │   :9092         │
                            └────────┬────────┘
                                     │
    ┌──────────────┬────────────────┼────────────────┬──────────────┐
    │              │                │                │              │
┌───▼───┐     ┌────▼────┐   ┌──────▼──────┐  ┌──────▼──────┐  ┌────▼────┐
│Notify │     │ Stats   │   │ PostgreSQL   │  │   Redis     │  │  MinIO  │
│:8086  │     │ :8087   │   │   :5432      │  │   :6379     │  │ :9000   │
└───────┘     └─────────┘   └─────────────┘  └─────────────┘  └─────────┘
```

### Событийная архитектура (Kafka)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Apache Kafka                                        │
│                    (Событийная шина сообщений)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  Topics:                                                                   │
│  • dn-quest.users.events     — События пользователей                        │
│  • dn-quest.quests.events   — События квестов                             │
│  • dn-quest.game.events     — События игрового процесса                   │
│  • dn-quest.teams.events    — События команд                              │
│  • dn-quest.notifications   — Уведомления                                 │
│  • dn-quest.files.events    — События файлов                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📱 Микросервисы

### 1. API Gateway (порт: 8080)

**Описание:** Единая точка входа для всех микросервисов. Обеспечивает маршрутизацию запросов, JWT-аутентификацию и rate limiting.

**Функции:**
- Маршрутизация запросов к соответствующим сервисам
- JWT-валидация и генерация токенов
- Rate limiting на основе Redis
- Circuit breaker (Resilience4j) для отказоустойчивости
- CORS конфигурация
- Логирование и трассировка

**Эндпоинты:**
```
/api/auth/**         → Authentication Service (:8081, /api/auth)
/api/users/**        → User Management Service (:8082, /api/users)
/api/quests/**       → Quest Management Service (:8083, /api/quests)
/api/game/**         → Game Engine Service (:8084, /api/game)
/api/teams/**        → Team Management Service (:8085, /api)
/api/notifications   → Notification Service (:8086, /api/notifications)
/api/statistics/**   → Statistics Service (:8087, /api/stats)
/api/files/**        → File Storage Service (:8088, /api/files)
```

### 2. Authentication Service (порт: 8081, context-path: /api/auth)

**Описание:** Сервис аутентификации и авторизации. Управляет регистрацией, входом и JWT-токенами.

**Основные возможности:**
- Регистрация пользователей
- Вход по логину/паролю
- Генерация и валидация JWT токенов (access + refresh)
- Сброс и изменение пароля
- Управление разрешениями (permissions)
- Интеграция с Kafka для событий пользователей

**База данных:** PostgreSQL (схема `auth`)

### 3. User Management Service (порт: 8082, context-path: /api/users)

**Описание:** Управление профилями пользователей и их данными.

**Основные возможности:**
- CRUD операции с профилями пользователей
- Обновление аватара и персональных данных
- Поиск пользователей
- История активности

**База данных:** PostgreSQL (схема `users`)

### 4. Quest Management Service (порт: 8083, context-path: /api/quests)

**Описание:** Создание и управление квестами.

**Основные возможности:**
- Создание и редактирование квестов
- Управление уровнями и заданиями
- Публикация и архивирование квестов
- Загрузка медиафайлов (через File Storage Service)
- Генерация QR-кодов для заданий

**База данных:** PostgreSQL (схема `quests`)

### 5. Game Engine Service (порт: 8084, context-path: /api/game)

**Описание:** Игровая логика и обработка кодов участников.

**Основные возможности:**
- Управление игровыми сессиями
- Обработка скан-кодов (QR-коды, NFC)
- Валидация ответов на задания
- Подсчёт очков и времени
- Лидерборды в реальном времени
- Интеграция с Redis для кэширования

**База данных:** PostgreSQL (схема `game`)

### 6. Team Management Service (порт: 8085, context-path: /api)

**Описание:** Управление командами участников.

**Основные возможности:**
- Создание и управление командами
- Приглашение участников
- Управление ролями в команде (лидер, участник)
- Командные статистики

**База данных:** PostgreSQL (схема `teams`)

### 7. Notification Service (порт: 8086, context-path: /api/notifications)

**Описание:** Сервис уведомлений через различные каналы.

**Основные возможности:**
- Email уведомления (SMTP)
- Telegram бот уведомления
- In-app уведомления
- Шаблонизация сообщений
- Очередь отправки через Kafka

**База данных:** PostgreSQL (схема `notifications`)

**Каналы:**
- 📧 Email (SMTP)
- 📱 Telegram Bot
- 🔔 In-App

### 8. Statistics Service (порт: 8087, context-path: /api/stats)

**Описание:** Сбор и анализ статистики игрового процесса.

**Основные возможности:**
- Статистика прохождения квестов
- Командные рейтинги
- Аналитика активности
- Экспорт отчётов

**База данных:** PostgreSQL (схема `statistics`)

### 9. File Storage Service (порт: 8088, context-path: /api/files)

**Описание:** Хранение файлов в MinIO (S3-совместимое хранилище).

**Основные возможности:**
- Загрузка файлов
- Генерация превью для изображений
- Управление файлами квестов
- Интеграция с MinIO

**Хранилище:** MinIO

---

## 🛠 Технологический стек

### Backend

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 21 | Язык программирования |
| Kotlin | 1.9.20 | Дополнительный язык |
| Spring Boot | 3.2.0 | Фреймворк |
| Spring Cloud Gateway | 4.1.x | API Gateway |
| Spring Data JPA | 3.2.x | ORM |
| PostgreSQL | 16 | Основная БД |
| Redis | 7 | Кэширование, сессии |
| Apache Kafka | 7.5.0 | Событийная шина |
| MinIO | latest | S3 хранилище |

### Frontend

| Технология | Версия | Назначение |
|------------|--------|------------|
| Vue.js | 3.x | Фреймворк |
| Vite | 5.x | Сборщик |
| Axios | ^1.6 | HTTP клиент |
| TailwindCSS | 3.x | Стилизация |

### Инфраструктура и мониторинг

| Технология | Версия | Назначение |
|------------|--------|------------|
| Docker | 24+ | Контейнеризация |
| Docker Compose | 2.20+ | Оркестрация |
| Grafana | 10.x | Мониторинг |
| Prometheus | 2.x | Метрики |
| Jaeger | 1.x | Трассировка |

---

## ▶️ Запуск

### Команды Makefile

```bash
# =============================================
# Сборка
# =============================================

make build               # Полная пересборка всех сервисов (без тестов)
make build-service      # Пересобрать один сервис: make build-service SERVICE=game-engine-service
make test               # Запустить все тесты
make check              # Проверка качества кода

# =============================================
# Docker Compose
# =============================================

make dev-up             # Запустить все микросервисы
make dev-infra          # Запустить только инфраструктуру
make dev-all           # Запустить инфраструктуру + все сервисы
make dev-down          # Остановить и удалить контейнеры
make dev-restart       # Полный перезапуск

# =============================================
# Управление сервисами
# =============================================

make logs SERVICE=xxx   # Логи сервиса: make logs SERVICE=api-gateway-dev
make status            # Статус всех контейнеров
make stats             # Использование ресурсов

# =============================================
# Консоли
# =============================================

make minio-console      # Открыть MinIO Console
make swagger           # Открыть Swagger UI
make pgadmin           # Открыть pgAdmin
```

### Ручной запуск

```bash
# Запуск инфраструктуры
docker compose -f docker-compose.dev.yml up -d postgres-dev redis-dev zookeeper-dev kafka-dev minio-dev

# Сборка jar файлов
./gradlew clean build -x test

# Запуск микросервисов
docker compose -f docker-compose.dev.yml up -d --build

# Остановка
docker compose -f docker-compose.dev.yml down -v --remove-orphans
```

### Запуск только инфраструктуры

```bash
make dev-infra
```

После запуска инфраструктуры доступны:
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- Kafka: localhost:9092
- MinIO: localhost:9000 (console: localhost:9001)

---

## ⚙️ Конфигурация

### Переменные окружения

Основные переменные находятся в файле `.env`. Для локальной разработки создайте `.env.local`:

```bash
# Копировать и настроить
cp .env .env.local

# Отредактировать
nano .env.local
```

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `POSTGRES_PASSWORD` | Пароль PostgreSQL | dn |
| `REDIS_HOST` | Хост Redis | localhost |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka серверы | kafka-dev:29092 |
| `MINIO_ACCESS_KEY` | MinIO ключ | minioadmin |
| `MINIO_SECRET_KEY` | MinIO секрет | minioadmin |
| `JWT_SECRET` | Секрет JWT | dnQuestSecretKeyForJWTTokenGenerationAndValidation2024 |
| `JWT_EXPIRATION` | Срок жизни access токена (мс) | 900000 (15 мин) |
| `JWT_REFRESH_EXPIRATION` | Срок жизни refresh токена (мс) | 604800000 (7 дней) |

### Frontend конфигурация

Настройки frontend находятся в `frontend/.env.development`:

```
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080
```

### Профили Spring

| Профиль | Назначение |
|---------|------------|
| `dev` | Локальная разработка |
| `test` | Тестирование |
| `prod` | Production |

---

## 📊 Мониторинг

### Доступные дашборды

| Инструмент | URL | Описание |
|------------|-----|-----------|
| Grafana | http://localhost:3001 | Метрики и дашборды |
| Jaeger | http://localhost:16686 | Распределённая трассировка |
| Prometheus | http://localhost:9090 | Сбор метрик |
| Kafka UI | http://localhost:8089 | Управление топиками |

### Метрики сервисов

- **API Gateway:** Rate limiting, количество запросов, время отклика
- **Authentication:** Входы, регистрации, ошибки аутентификации
- **Game Engine:** Активные сессии, количество игроков
- **Database:** Запросы, время выполнения, соединения

### Проверка здоровья сервисов

```bash
make health
```

или через curl:

```bash
curl http://localhost:8080/api/actuator/health
curl http://localhost:8081/auth/actuator/health
curl http://localhost:8082/users/actuator/health
```

---

## 🔒 Безопасность

### Checklist для Production

- [ ] Изменить все пароли по умолчанию
- [ ] Настроить HTTPS (SSL/TLS сертификаты)
- [ ] Сменить `JWT_SECRET` на безопасное значение (минимум 256 бит)
- [ ] Настроить firewall (открыть только необходимые порты)
- [ ] Включить мониторинг и алерты
- [ ] Настроить резервное копирование БД
- [ ] Провести нагрузочное тестирование
- [ ] Настроить логирование аудита

### Порты для firewall

```
80, 443     - HTTP/HTTPS
3000        - Frontend
8080        - API Gateway
5432        - PostgreSQL
6379        - Redis
9092        - Kafka
```

---

## 📂 Структура проекта

```
dn-quest/
├── api-gateway/           # API Gateway сервис
├── authentication-service/ # Сервис аутентификации
├── user-management-service/ # Управление пользователями
├── quest-management-service/ # Управление квестами
├── game-engine-service/  # Игровой движок
├── team-management-service/ # Управление командами
├── notification-service/ # Сервис уведомлений
├── statistics-service/  # Сервис статистики
├── file-storage-service/ # Хранение файлов
├── dn-quest-shared/      # Общие библиотеки и компоненты
├── frontend/             # Vue.js фронтенд
├── docker/               # Конфигурации Docker
├── docs/                 # Документация
│   └── user-guide.md     # Руководство пользователя
├── Makefile              # Команды для разработки
├── docker-compose.dev.yml # Docker Compose конфигурация
└── .env                  # Переменные окружения
```

---

## 🤝 Лицензия

MIT License

---

## 👨‍💻 Автор

Denis Neverov
