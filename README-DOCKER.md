# DN Quest - Docker Quick Start

## 🚀 Быстрый старт

### 1. Инициализация
```bash
chmod +x dn-quest.sh
./dn-quest.sh init
```

### 2. Запуск
```bash
# Разработка
./dn-quest.sh start -e dev

# Продакшен
./dn-quest.sh start -e prod

# Полный стек с мониторингом
./dn-quest.sh start -e full
```

### 3. Доступ к сервисам
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **API Docs**: http://localhost:8080/swagger-ui.html

## 📋 Основные команды

```bash
# Управление
./dn-quest.sh start          # Запуск
./dn-quest.sh stop           # Остановка
./dn-quest.sh restart        # Перезапуск
./dn-quest.sh status         # Статус
./dn-quest.sh logs           # Логи

# Сервисы
./dn-quest.sh restart-service auth-service
./dn-quest.sh logs api-gateway -f

# Утилиты
./dn-quest.sh build          # Пересборка
./dn-quest.sh clean          # Очистка
```

## 🏗️ Архитектура

### Микросервисы
- **API Gateway** (8080) - Маршрутизация
- **Authentication** (8081) - Аутентификация
- **User Management** (8082) - Пользователи
- **Quest Management** (8083) - Квесты
- **Game Engine** (8084) - Игровой движок
- **Team Management** (8085) - Команды
- **Notifications** (8086) - Уведомления
- **Statistics** (8087) - Статистика
- **File Storage** (8088) - Файлы
- **Frontend** (3000) - Vue.js приложение

### Инфраструктура
- **PostgreSQL** - Базы данных
- **Redis** - Кэширование
- **Kafka** - Очереди сообщений
- **MinIO** - Хранилище файлов
- **Nginx** - Load balancer

### Мониторинг (full)
- **Prometheus** (9090) - Метрики
- **Grafana** (3001) - Дашборды
- **Jaeger** (16686) - Трейсинг
- **ELK Stack** - Логирование

## 🌍 Окружения

| Окружение | Команда | Описание |
|-----------|---------|----------|
| **dev** | `./dn-quest.sh start -e dev` | Разработка с hot reload |
| **prod** | `./dn-quest.sh start -e prod` | Продакшен с оптимизациями |
| **full** | `./dn-quest.sh start -e full` | Полный стек с мониторингом |
| **test** | `./dn-quest.sh start -e test` | Тестирование с mock сервисами |

## 🔧 Конфигурация

Файлы окружения:
- `.env.development` - Разработка
- `.env.production` - Продакшен
- `.env.full` - Полный стек
- `.env.testing` - Тестирование

Основные настройки:
```bash
# Порты
API_GATEWAY_PORT=8080
FRONTEND_PORT=3000

# База данных
POSTGRES_USER=dn
POSTGRES_PASSWORD=dn

# JWT
JWT_SECRET=your-secret-key
```

## 📊 Мониторинг

### Grafana
- URL: http://localhost:3001
- Логин: `admin`
- Пароль: `admin`

### Prometheus
- URL: http://localhost:9090

### Jaeger
- URL: http://localhost:16686

## 🐛 Траблшутинг

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

## 📚 Документация

- [Полное руководство](DOCKER_SETUP_GUIDE.md)
- [API документация](http://localhost:8080/swagger-ui.html)
- [Микросервисы](README-MICROSERVICES.md)

## 🔒 Безопасность

Для продакшена:
1. Измените пароли в `.env.production`
2. Настройте HTTPS
3. Включите firewall
4. Обновите образы

## 💡 Советы

- Используйте `dev` окружение для разработки
- `full` окружение требует больше ресурсов
- Проверяйте статус перед обращением в поддержку
- Регулярно делайте бэкапы в продакшене

---

**DN Quest** - Микросервисная платформа для управления квестами