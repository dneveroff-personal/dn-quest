# DN Quest - Платформа для онлайн-квестов

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

DN Quest — это микросервисная платформа для создания и прохождения онлайн-квестов в команде или одиночку.

## 🚀 Быстрый старт

### Требования

- **Docker** 20.10+ и **Docker Compose** 2.0+
- **8GB+ RAM** (минимум)
- **20GB+ свободного диска**

### Запуск на локальной машине (Development)

```bash
# 1. Сделать скрипт исполняемым
chmod +x dn-quest.sh

# 2. Инициализировать проект
./dn-quest.sh init

# 3. Запустить в режиме разработки
./dn-quest.sh start -e dev

# Проверить статус
./dn-quest.sh status
```

После запуска сервисы будут доступны:
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8089
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)

### Запуск в Production режиме

```bash
# 1. Настроить переменные окружения
cp .env.production .env
# Отредактировать .env с безопасными значениями паролей и секретов

# 2. Собрать и запустить
./dn-quest.sh build
./dn-quest.sh start -e prod

# Или использовать docker-compose напрямую
docker compose -f docker-compose.production.yml up -d
```

## 📋 Основные команды

```bash
# Управление сервисами
./dn-quest.sh start          # Запуск (используйте -e dev/prod/full для разных окружений)
./dn-quest.sh stop          # Остановка
./dn-quest.sh restart       # Перезапуск
./dn-quest.sh status        # Статус сервисов
./dn-quest.sh logs          # Просмотр логов

# Управление конкретным сервисом
./dn-quest.sh restart-service api-gateway
./dn-quest.sh logs api-gateway -f

# Сборка
./dn-quest.sh build         # Пересобрать все сервисы
./dn-quest.sh clean         # Очистить контейнеры и volumes
```

## 🏗️ Архитектура

### Микросервисы

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

### Инфраструктура

- **PostgreSQL** (8 баз данных) — основное хранилище
- **Redis** — кэширование и сессии
- **Kafka** — асинхронная коммуникация
- **MinIO** — S3-совместимое хранилище файлов
- **Nginx** — reverse proxy и load balancer

### Мониторинг (Full окружение)

| Сервис | Порт | Описание |
|--------|------|----------|
| **Prometheus** | 9090 | Сбор метрик |
| **Grafana** | 3001 | Визуализация (admin/admin) |
| **Jaeger** | 16686 | Распределенный трейсинг |
| **Kibana** | 5601 | Логирование |

## 🌍 Окружения

| Окружение | Команда | Описание |
|-----------|---------|----------|
| **dev** | `./dn-quest.sh start -e dev` | Разработка с hot reload |
| **prod** | `./dn-quest.sh start -e prod` | Production с оптимизациями |
| **full** | `./dn-quest.sh start -e full` | Полный стек с мониторингом |
| **test** | `./dn-quest.sh start -e test` | Тестирование |

## 🔧 Конфигурация

### Файлы окружений

- `.env.development` — настройки для разработки
- `.env.production` — настройки для продакшена
- `.env.full` — полный стек с мониторингом
- `.env.testing` — для тестирования

### Основные переменные

```bash
# База данных
POSTGRES_USER=dn
POSTGRES_PASSWORD=dn  # Изменить в production!

# JWT
JWT_SECRET=your-secret-key  # Изменить в production!

# Порты
API_GATEWAY_PORT=8080
FRONTEND_PORT=3000
```

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
├── scripts/                 # Скрипты управления
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

## 🐛 Troubleshooting

```bash
# Проверка статуса
./dn-quest.sh status -h -d

# Логи с ошибками
./dn-quest.sh logs | grep ERROR

# Перезапуск сервиса
./dn-quest.sh restart-service api-gateway

# Полная очистка
./dn-quest.sh clean
```

### Частые проблемы

1. **Порты заняты** — измените порты в `.env` файле
2. **Недостаточно памяти** — увеличьте Docker memory limits
3. **Базы данных не стартуют** — проверьте права на volumes

## 🤝 Вклад в проект

1. Fork репозитория
2. Создайте feature branch
3. Внесите изменения
4. Создайте Pull Request

## 📄 Лицензия

MIT License

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

**DN Quest** — современная платформа для онлайн-квестов с микросервисной архитектурой

## Команды для разработки:
- Показать статус запущенных сервисов 
  > docker ps --format "table {{.Names}}\t{{.Status}}"

- Билд jar файлов 
  > ./gradlew clean build -x test

- Запуск докер контейнеров 
  > docker compose -f docker-compose.dev.yml up -d --build
  
- Запуск последовательно:
  - №1 ТОЛЬКО контейнеров служб
  >   docker compose -f docker-compose.dev.yml up -d postgres-dev redis-dev zookeeper-dev kafka-dev minio-dev
  - №2 authentication-service-dev
  >   docker compose -f docker-compose.dev.yml up -d authentication-service-dev
  - №3 user-management-service-dev и file-storage-service-dev
  > docker compose -f docker-compose.dev.yml up -d user-management-service-dev file-storage-service-dev
- Правильная остановка всех контейнеров
  > docker compose -f docker-compose.dev.yml down -v --remove-orphans

- Перезапуск определенного контейнера 
  > docker compose -f docker-compose.dev.yml up -d frontend-dev

- Дроп БД для билда с нова
  > docker compose -f docker-compose.dev.yml down -v && docker volume rm dn-quest-dev_postgres_dev_data 2>/dev/null || true
