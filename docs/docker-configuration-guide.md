# DN Quest - Docker Configuration Guide

## Обзор

Этот документ описывает Docker конфигурации для микросервисной архитектуры DN Quest, включая оптимизированные Dockerfile, скрипты сборки и развертывания.

## Структура Docker конфигураций

### Микросервисы

Проект включает следующие микросервисы с Docker конфигурациями:

- **api-gateway** (порт 8080) - API Gateway
- **authentication-service** (порт 8081) - Сервис аутентификации
- **user-management-service** (порт 8082) - Управление пользователями
- **quest-management-service** (порт 8083) - Управление квестами
- **game-engine-service** (порт 8084) - Игровой движок
- **team-management-service** (порт 8085) - Управление командами
- **notification-service** (порт 8086) - Уведомления
- **statistics-service** (порт 8087) - Статистика
- **file-storage-service** (порт 8088) - Хранение файлов

### Инфраструктурные сервисы

- **PostgreSQL** (8 экземпляров) - Базы данных для каждого микросервиса
- **Redis** - Кэширование и сессии
- **Kafka + Zookeeper** - Event-driven коммуникация
- **MinIO** - Object storage для файлов
- **Kafka UI** - Управление Kafka

## Оптимизированные Dockerfile

Все Dockerfile используют multi-stage builds с Alpine Linux для минимизации размера образов:

### Ключевые особенности:

1. **Multi-stage builds** - Разделение сборки и рантайма
2. **Alpine Linux** - Минимальный размер образа (~50MB)
3. **Non-root пользователь** - Безопасность (user: 1001)
4. **Health checks** - Мониторинг состояния сервисов
5. **Оптимизированные JVM опции** - Настройка для контейнеров

### Пример структуры Dockerfile:

```dockerfile
# Build stage
FROM openjdk:21-jdk-alpine AS builder
# ... сборка приложения

# Production stage
FROM openjdk:21-jre-alpine
# ... настройка рантайма
```

## .dockerignore файлы

Каждый сервис имеет `.dockerignore` для исключения ненужных файлов:

- Gradle кэш и артефакты
- IDE файлы (.idea, .vscode)
- Логи и временные файлы
- Документация
- Конфигурационные файлы окружения

## Jib конфигурация

Все сервисы настроены для использования Google Jib:

```kotlin
jib {
    from {
        image = "openjdk:21-jre-alpine"
    }
    to {
        image = "dn-quest/service-name:1.0.0"
        tags = setOf("latest", "1.0.0")
    }
    container {
        // Оптимизированные настройки
    }
}
```

## Переменные окружения

### Файлы окружения:

- `.env.example` - Шаблон с всеми переменными
- `.env.development` - Настройки для разработки
- `.env.testing` - Настройки для тестирования
- `.env.production` - Настройки для продакшена

### Ключевые переменные:

```bash
# Глобальные
COMPOSE_PROJECT_NAME=dn-quest
SPRING_PROFILES_ACTIVE=dev
ENVIRONMENT=development

# Базы данных
POSTGRES_USER=dn
POSTGRES_PASSWORD=dn
POSTGRES_AUTH_DB=dnquest_auth
# ... другие базы данных

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Сервисы
REDIS_HOST=redis
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
MINIO_ENDPOINT=http://minio:9000
```

## Docker Compose конфигурации

### Доступные файлы:

1. **docker-compose.yml** - Базовая конфигурация
2. **docker-compose.optimized.yml** - Оптимизированная для разработки
3. **docker-compose.production.yml** - Production конфигурация
4. **docker-compose.dev.yml** - Для разработки
5. **docker-compose.kafka.yml** - Только Kafka сервисы

### Оптимизированная конфигурация включает:

- **Health checks** для всех сервисов
- **Resource limits** - Ограничения памяти и CPU
- **Restart policies** - Политики перезапуска
- **Logging** - Настройки логирования
- **Network isolation** - Изоляция сетей
- **Volume management** - Управление томами

## Скрипты сборки и развертывания

### build-all-services.sh

Сборка всех сервисов:

```bash
# Сборка с Docker
./scripts/build-all-services.sh docker dn-quest 1.0.0

# Сборка с Jib
./scripts/build-all-services.sh jib dn-quest 1.0.0
```

### publish-images.sh

Публикация образов в registry:

```bash
# Публикация с настройками по умолчанию
./scripts/publish-images.sh

# Публикация в custom registry
./scripts/publish-images.sh my-registry.com/dn-quest 1.2.0
```

### run-local.sh

Запуск локального окружения:

```bash
# Запуск с настройками по умолчанию
./scripts/run-local.sh

# Запуск с custom compose файлом
./scripts/run-local.sh docker-compose.optimized.yml dev true
```

## Использование

### Быстрый старт для разработки:

```bash
# 1. Копировать файл окружения
cp .env.development .env

# 2. Собрать все сервисы
./scripts/build-all-services.sh docker

# 3. Запустить локально
./scripts/run-local.sh docker-compose.optimized.yml
```

### Production развертывание:

```bash
# 1. Настроить production переменные
cp .env.production .env
# Отредактировать .env с production значениями

# 2. Собрать образы
./scripts/build-all-services.sh docker dn-quest 1.0.0

# 3. Опубликовать в registry
./scripts/publish-images.sh your-registry.com/dn-quest 1.0.0

# 4. Запустить production
docker-compose -f docker-compose.production.yml up -d
```

### Тестирование:

```bash
# 1. Использовать тестовое окружение
cp .env.testing .env

# 2. Запустить тестовые сервисы
docker-compose -f docker-compose.optimized.yml --profile testing up -d

# 3. Запустить тесты
./gradlew test
```

## Мониторинг и отладка

### Health checks:

Все сервисы имеют health checks:

```bash
# Проверить состояние всех сервисов
docker-compose ps

# Проверить health конкретного сервиса
curl http://localhost:8080/actuator/health
```

### Логи:

```bash
# Логи всех сервисов
docker-compose logs -f

# Логи конкретного сервиса
docker-compose logs -f api-gateway
```

### Метрики:

```bash
# Prometheus метрики
curl http://localhost:8080/actuator/prometheus

# Kafka UI
http://localhost:8089

# MinIO Console
http://localhost:9001
```

## Оптимизация производительности

### Настройки JVM:

```bash
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

### Resource limits:

```yaml
deploy:
  resources:
    limits:
      memory: 512M
      cpus: '0.5'
    reservations:
      memory: 256M
      cpus: '0.25'
```

### Database pooling:

```bash
DATABASE_POOL_SIZE=20
DATABASE_MAX_POOL_SIZE=50
```

## Безопасность

### Non-root пользователь:

Все контейнеры запускаются под пользователем `dnquest` (UID: 1001)

### Секреты:

- Используйте переменные окружения для секретов
- Не храните секреты в Git
- Используйте Docker secrets в production

### Network isolation:

```yaml
networks:
  dn-quest-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

## Troubleshooting

### Общие проблемы:

1. **Порты уже заняты** - Измените порты в .env файле
2. **Недостаточно памяти** - Увеличьте limits в docker-compose
3. **Базы данных не стартуют** - Проверьте права доступа к volumes
4. **Сервисы не видят друг друга** - Убедитесь что они в одной сети

### Полезные команды:

```bash
# Очистить Docker
docker system prune -a

# Пересобрать образы
docker-compose build --no-cache

# Проверить конфигурацию
docker-compose config

# Перезапустить сервис
docker-compose restart service-name
```

## Best Practices

1. **Используйте .dockerignore** для минимизации контекста сборки
2. **Мониторьте размер образов** - стремитесь к < 200MB
3. **Используйте health checks** для всех сервисов
4. **Настраивайте resource limits** для предотвращения OOM
5. **Используйте отдельные сети** для изоляции
6. **Ведите логи** с ротацией
7. **Регулярно обновляйте** базовые образы
8. **Используйте сканирование** уязвимостей образов

## Дальнейшая оптимизация

1. **Kubernetes deployment** - Используйте Helm charts
2. **CI/CD integration** - Автоматизируйте сборку и развертывание
3. **Monitoring stack** - Добавьте Prometheus + Grafana
4. **Log aggregation** - Используйте ELK stack
5. **Backup strategy** - Настройте резервное копирование
6. **Security scanning** - Интегрируйте сканеры уязвимостей