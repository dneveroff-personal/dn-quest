# DN Quest - Docker Setup Guide

## Обзор

DN Quest - это микросервисная архитектура для управления квестами, построенная на Java Spring Boot, Vue.js и современных инфраструктурных компонентах. Этот гид поможет вам настроить и запустить всю систему с помощью Docker.

## Архитектура

### Микросервисы

- **API Gateway** (порт 8080) - Шлюз для маршрутизации запросов
- **Authentication Service** (порт 8081) - Сервис аутентификации и авторизации
- **User Management Service** (порт 8082) - Управление пользователями
- **Quest Management Service** (порт 8083) - Управление квестами
- **Game Engine Service** (порт 8084) - Игровой движок
- **Team Management Service** (порт 8085) - Управление командами
- **Notification Service** (порт 8086) - Уведомления
- **Statistics Service** (порт 8087) - Статистика и аналитика
- **File Storage Service** (порт 8088) - Хранение файлов
- **Frontend** (порт 3000) - Vue.js приложение

### Инфраструктура

- **PostgreSQL** - Базы данных для каждого сервиса
- **Redis** - Кэширование и сессии
- **Kafka** - Очередь сообщений
- **MinIO** - S3-совместимое хранилище файлов
- **Nginx** - Load balancer и reverse proxy

### Мониторинг (в full окружении)

- **Prometheus** (порт 9090) - Сбор метрик
- **Grafana** (порт 3001) - Визуализация метрик
- **Jaeger** (порт 16686) - Распределенный трейсинг
- **ELK Stack** - Логирование (Elasticsearch, Kibana, Logstash)

## Требования

- Docker 20.10+
- Docker Compose 2.0+
- Минимум 8GB RAM
- Минимум 20GB свободного дискового пространства

## Быстрый старт

### 1. Инициализация проекта

```bash
# Сделайте главный скрипт исполняемым
chmod +x dn-quest.sh

# Инициализируйте проект
./dn-quest.sh init
```

### 2. Запуск в режиме разработки

```bash
# Запуск всех сервисов в режиме разработки
./dn-quest.sh start -e dev

# Проверка статуса
./dn-quest.sh status

# Просмотр логов
./dn-quest.sh logs
```

### 3. Доступ к сервисам

После запуска сервисы будут доступны по следующим адресам:

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8089
- **MinIO Console**: http://localhost:9001

## Окружения

### Development (dev)

Оптимально для разработки и тестирования:

```bash
./dn-quest.sh start -e dev
```

**Особенности:**
- Hot reload для frontend
- Debug порты для Java сервисов
- Увеличенные лимиты для разработки
- Подробное логирование
- Отключенные ограничения безопасности

### Production (prod)

Для продакшен окружения:

```bash
./dn-quest.sh start -e prod
```

**Особенности:**
- Оптимизированные настройки производительности
- Усиленная безопасность
- Resource limits
- Минимальное логирование
- Load balancing через Nginx

### Full Stack (full)

Полный стек с мониторингом:

```bash
./dn-quest.sh start -e full
```

**Особенности:**
- Все сервисы разработки
- Полный стек мониторинга
- Распределенный трейсинг
- Централизованное логирование
- Дашборды Grafana

### Testing (test)

Для автоматического тестирования:

```bash
./dn-quest.sh start -e test
```

**Особенности:**
- Изолированная тестовая среда
- Mock сервисы
- Тестовые данные
- Автоматическая очистка

## Управление сервисами

### Основные команды

```bash
# Запуск всех сервисов
./dn-quest.sh start

# Остановка всех сервисов
./dn-quest.sh stop

# Перезапуск всех сервисов
./dn-quest.sh restart

# Проверка статуса
./dn-quest.sh status

# Просмотр логов
./dn-quest.sh logs

# Перестроить образы
./dn-quest.sh build

# Очистка системы
./dn-quest.sh clean
```

### Управление отдельными сервисами

```bash
# Перезапуск конкретного сервиса
./dn-quest.sh restart-service authentication-service

# Просмотр логов сервиса
./dn-quest.sh logs api-gateway -f

# Просмотр статуса с health checks
./dn-quest.sh status -h -d
```

### Просмотр логов

```bash
# Все логи
./dn-quest.sh logs

# Логи конкретного сервиса
./dn-quest.sh logs authentication-service

# Следить за логами в реальном времени
./dn-quest.sh logs -f

# Логи за последние 100 строк
./dn-quest.sh logs -t 100

# Логи за последние 2 часа
./dn-quest.sh logs -s 2h

# Логи с временными метками
./dn-quest.sh logs --timestamps
```

## Конфигурация

### Environment переменные

Каждое окружение использует свой файл конфигурации:

- `.env.development` - для разработки
- `.env.production` - для продакшена
- `.env.full` - для полного стека
- `.env.testing` - для тестирования

Основные параметры:

```bash
# Глобальные настройки
COMPOSE_PROJECT_NAME=dn-quest
ENVIRONMENT=development

# Порты сервисов
API_GATEWAY_PORT=8080
AUTHENTICATION_SERVICE_PORT=8081
# ... и т.д.

# База данных
POSTGRES_USER=dn
POSTGRES_PASSWORD=dn

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

### Настройка для продакшена

1. **Измените пароли и секреты:**

```bash
# В .env.production
POSTGRES_PASSWORD=your_secure_password
JWT_SECRET=your_very_long_and_secure_secret_key
MINIO_ACCESS_KEY=your_minio_access_key
MINIO_SECRET_KEY=your_minio_secret_key
```

2. **Настройте домены:**

```bash
CORS_ALLOWED_ORIGINS=https://yourdomain.com
VITE_API_BASE_URL=https://api.yourdomain.com/api
```

3. **Настройте email:**

```bash
SMTP_HOST=your-smtp-server.com
SMTP_USERNAME=your-email@domain.com
SMTP_PASSWORD=your-email-password
```

## Мониторинг

### Prometheus

Доступ: http://localhost:9090

- Метрики всех сервисов
- Целевые системы для мониторинга
- Alerting правила

### Grafana

Доступ: http://localhost:3001

- Логин: admin
- Пароль: admin (измените в продакшене)

**Предустановленные дашборды:**
- DN Quest Overview
- Service Performance
- Infrastructure Metrics
- Business Metrics

### Jaeger

Доступ: http://localhost:16686

- Распределенный трейсинг
- Визуализация запросов между сервисами
- Анализ производительности

### ELK Stack

- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Logstash**: Порт 5044

## Траблшутинг

### Проверка статуса сервисов

```bash
# Детальный статус
./dn-quest.sh status -d -h

# Проверка конкретного сервиса
docker compose -f docker-compose.yml ps authentication-service
```

### Просмотр логов

```bash
# Логи с ошибками
./dn-quest.sh logs | grep ERROR

# Логи конкретного сервиса
./dn-quest.sh logs authentication-service -f
```

### Перезапуск сервисов

```bash
# Перезапуск с пересборкой
./dn-quest.sh restart-service api-gateway -b

# Полный перезапуск
./dn-quest.sh stop && ./dn-quest.sh start
```

### Очистка

```bash
# Полная очистка (удалит все данные!)
./dn-quest.sh clean

# Очистка только контейнеров
docker compose-f docker-compose.yml down -v

# Очистка образов
docker system prune -a
```

### Частые проблемы

1. **Порты уже заняты:**

```bash
# Проверка занятых портов
netstat -tulpn | grep :8080

# Изменение портов в .env файле
API_GATEWAY_PORT=8081
```

2. **Недостаточно памяти:**

```bash
# Проверка использования памяти
docker stats

# Очистка неиспользуемых контейнеров
docker system prune -a
```

3. **Проблемы с базой данных:**

```bash
# Проверка статуса PostgreSQL
./dn-quest.sh logs postgres

# Пересоздание базы данных
docker compose-f docker-compose.yml down -v
docker compose-f docker-compose.yml up -d postgres
```

## Разработка

### Hot Reload

Frontend поддерживает hot reload автоматически. Для Java сервисов:

```bash
# Пересборка конкретного сервиса
./dn-quest.sh restart-service quest-management-service -b
```

### Debug

Java сервисы доступны для отладки на портах:

- Authentication Service: 5001
- User Management: 5002
- Quest Management: 5003
- Game Engine: 5004
- Team Management: 5005
- Notification Service: 5006
- Statistics Service: 5007
- File Storage: 5008

### Тестирование

```bash
# Запуск тестового окружения
./dn-quest.sh start -e test

# Запуск интеграционных тестов
./scripts/run-integration-tests.sh

# Запуск нагрузочных тестов
./scripts/run-load-tests.sh
```

## Бэкап и восстановление

### Бэкап баз данных

```bash
# Создание бэкапа
./scripts/backup-databases.sh

# Восстановление из бэкапа
./scripts/restore-databases.sh backup_file.sql
```

### Бэкап файлов

```bash
# Бэкап MinIO
./scripts/backup-minio.sh

# Восстановление MinIO
./scripts/restore-minio.sh backup_file.tar
```

## Безопасность

### Рекомендации для продакшена

1. **Измените все пароли и секреты по умолчанию**
2. **Используйте HTTPS**
3. **Настройте firewall**
4. **Регулярно обновляйте образы**
5. **Включите мониторинг безопасности**
6. **Настройте бэкапы**

### SSL/TLS

```bash
# В .env.production
SSL_ENABLED=true
SSL_KEYSTORE_PATH=/etc/ssl/certs/dnquest.p12
SSL_KEYSTORE_PASSWORD=your_keystore_password
```

## Производительность

### Оптимизация

1. **Resource limits** в docker-compose.prod.yml
2. **Connection pooling** для баз данных
3. **Кэширование** через Redis
4. **Load balancing** через Nginx

### Мониторинг производительности

- Используйте Grafana дашборды
- Настройте alerting в Prometheus
- Мониторьте ключевые метрики

## CI/CD

### GitHub Actions

Пример конфигурации в `.github/workflows/docker.yml`:

```yaml
name: Build and Deploy
on:
  push:
    branches: [main]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build and push Docker images
        run: |
          ./scripts/build-all-services.sh
          ./scripts/push-images.sh
      - name: Deploy to production
        run: |
          ./dn-quest.sh start -e prod
```

## Поддержка

### Полезные команды

```bash
# Показать все доступные команды
./dn-quest.sh help

# Показать информацию о системе
./dn-quest.sh status -v

# Проверить health checks
./dn-quest.sh status -h
```

### Логи и документация

- Логи всех сервисов: `./dn-quest.sh logs`
- API документация: http://localhost:8080/swagger-ui.html
- Grafana дашборды: http://localhost:3001
- Этот документ: `DOCKER_SETUP_GUIDE.md`

### Контакты

Для вопросов и поддержки:
- GitHub Issues: [repository issues]
- Документация: `docs/`
- Wiki: [repository wiki]