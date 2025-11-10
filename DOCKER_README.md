# DN Quest - Docker Quick Start

## 🚀 Быстрый старт

### Требования
- Docker 20.10+
- Docker Compose 2.0+
- 8GB+ RAM
- Java 21 (для локальной разработки)

### 1. Клонирование и настройка

```bash
git clone <repository-url>
cd dn-quest

# Копировать файл окружения для разработки
cp .env.development .env
```

### 2. Сборка и запуск

```bash
# Собрать все сервисы
./scripts/build-all-services.sh docker

# Запустить локальное окружение
./scripts/run-local.sh docker-compose.optimized.yml
```

### 3. Доступ к сервисам

| Сервис | URL | Порт |
|--------|-----|------|
| Frontend | http://localhost:3000 | 3000 |
| API Gateway | http://localhost:8080 | 8080 |
| Authentication | http://localhost:8081 | 8081 |
| Kafka UI | http://localhost:8089 | 8089 |
| MinIO Console | http://localhost:9001 | 9001 |

## 📋 Доступные команды

### Сборка
```bash
# Собрать все сервисы с Docker
./scripts/build-all-services.sh docker

# Собрать с Jib (без Docker daemon)
./scripts/build-all-services.sh jib

# Собрать с custom тегами
./scripts/build-all-services.sh docker my-registry/dn-quest 1.0.0
```

### Публикация
```bash
# Публиковать в registry по умолчанию
./scripts/publish-images.sh

# Публиковать в custom registry
./scripts/publish-images.sh my-registry.com/dn-quest 1.0.0
```

### Запуск
```bash
# Запустить с оптимизированной конфигурацией
./scripts/run-local.sh docker-compose.optimized.yml

# Запустить production конфигурацию
./scripts/run-local.sh docker-compose.production.yml production true

# Запустить только инфраструктуру
docker-compose -f docker-compose.kafka.yml up -d
```

## 🔧 Конфигурации окружения

### Доступные окружения
- **development** - Для локальной разработки
- **testing** - Для тестирования
- **production** - Для продакшена

### Переключение окружения
```bash
# Для разработки
cp .env.development .env

# Для тестирования
cp .env.testing .env

# Для продакшена
cp .env.production .env
```

## 🏗️ Структура Docker конфигураций

```
dn-quest/
├── scripts/
│   ├── build-all-services.sh    # Сборка всех сервисов
│   ├── publish-images.sh        # Публикация образов
│   └── run-local.sh            # Локальный запуск
├── docker-compose.yml           # Базовая конфигурация
├── docker-compose.optimized.yml # Оптимизированная для разработки
├── docker-compose.production.yml # Production конфигурация
├── docker-compose.dev.yml       # Для разработки
├── docker-compose.kafka.yml     # Только Kafka сервисы
├── .env.example                 # Шаблон переменных
├── .env.development             # Переменные для разработки
├── .env.testing                 # Переменные для тестирования
├── .env.production              # Переменные для продакшена
└── docs/
    └── docker-configuration-guide.md # Подробная документация
```

## 🐳 Микросервисы

| Сервис | Dockerfile | Порт | Health Check |
|--------|------------|------|--------------|
| api-gateway | ✅ | 8080 | `/actuator/health` |
| authentication-service | ✅ | 8081 | `/actuator/health` |
| user-management-service | ✅ | 8082 | `/actuator/health` |
| quest-management-service | ✅ | 8083 | `/actuator/health` |
| game-engine-service | ✅ | 8084 | `/actuator/health` |
| team-management-service | ✅ | 8085 | `/actuator/health` |
| notification-service | ✅ | 8086 | `/actuator/health` |
| statistics-service | ✅ | 8087 | `/actuator/health` |
| file-storage-service | ✅ | 8088 | `/actuator/health` |

## 🔍 Мониторинг и отладка

### Проверка состояния
```bash
# Статус всех сервисов
docker-compose ps

# Health check конкретного сервиса
curl http://localhost:8080/actuator/health

# Логи всех сервисов
docker-compose logs -f

# Логи конкретного сервиса
docker-compose logs -f api-gateway
```

### Метрики
```bash
# Prometheus метрики
curl http://localhost:8080/actuator/prometheus

# Статистика Docker
docker stats
```

## 🛠️ Полезные команды

### Очистка
```bash
# Очистить все Docker ресурсы
docker system prune -a

# Удалить все образы проекта
docker rmi $(docker images "dn-quest*" -q)

# Удалить все контейнеры
docker rm $(docker ps -aq)
```

### Пересборка
```bash
# Пересобрать все образы
docker-compose build --no-cache

# Пересобрать конкретный сервис
docker-compose build --no-cache api-gateway
```

### Перезапуск
```bash
# Перезапустить все сервисы
docker-compose restart

# Перезапустить конкретный сервис
docker-compose restart api-gateway
```

## 🚨 Troubleshooting

### Частые проблемы

1. **Порты заняты**
   ```bash
   # Найти процесс использующий порт
   lsof -i :8080
   # Изменить порт в .env файле
   ```

2. **Недостаточно памяти**
   ```bash
   # Увеличить Docker memory limits
   # Или уменьшить memory limits в docker-compose
   ```

3. **Базы данных не стартуют**
   ```bash
   # Проверить права доступа к volumes
   sudo chown -R 1001:1001 ./data
   ```

4. **Сервисы не видят друг друга**
   ```bash
   # Проверить сеть
   docker network ls
   docker network inspect dn-quest-network
   ```

### Полезные команды для отладки
```bash
# Проверить конфигурацию
docker-compose config

# Войти в контейнер
docker-compose exec api-gateway sh

# Проверить логи конкретного сервиса
docker-compose logs --tail=100 api-gateway

# Проверить ресурсное потребление
docker stats --no-stream
```

## 📚 Дополнительная документация

- [Подробное руководство по Docker конфигурациям](docs/docker-configuration-guide.md)
- [Архитектура микросервисов](docs/microservices-architecture.md)
- [Интеграция с Kafka](docs/kafka-integration.md)

## 🏆 Best Practices

1. **Всегда используйте .dockerignore** для оптимизации сборки
2. **Мониторьте размер образов** - стремитесь к < 200MB
3. **Используйте health checks** для всех сервисов
4. **Настраивайте resource limits** для стабильности
5. **Используйте отдельные сети** для изоляции
6. **Регулярно обновляйте** базовые образы
7. **Не храните секреты** в Git - используйте переменные окружения

## 🆘 Поддержка

Если возникли проблемы:

1. Проверьте [подробную документацию](docs/docker-configuration-guide.md)
2. Убедитесь что все требования выполнены
3. Проверьте логи сервисов
4. Создайте issue с подробным описанием проблемы

---

**Готово к использованию!** 🎉

Теперь у вас есть полностью настроенная Docker среда для DN Quest микросервисов.