# Notification Service

Микросервис для управления уведомлениями в архитектуре DN Quest.

## Обзор

Notification Service предоставляет централизованную систему для отправки уведомлений через различные каналы:
- Email уведомления (SMTP)
- Push уведомления (Firebase/FCM)
- In-app уведомления (WebSocket)
- Telegram уведомления (Telegram Bot API)
- SMS уведомления

## Архитектура

### Основные компоненты

1. **Notification Channels** - реализация паттерна Strategy для разных каналов доставки
2. **Queue System** - асинхронная обработка уведомлений с retry логикой
3. **Template Management** - система управления шаблонами с мультиязычной поддержкой
4. **User Preferences** - управление предпочтениями пользователей
5. **Analytics & Monitoring** - сбор метрик и мониторинг производительности
6. **Rate Limiting** - защита от спама и перегрузок

### Поток обработки уведомлений

```
Event → Validation → Queue → Channel Manager → Delivery → Analytics
```

## API Эндпоинты

### Управление уведомлениями

- `POST /api/notifications/send` - отправка уведомления
- `GET /api/notifications/{userId}` - история уведомлений
- `PUT /api/notifications/{id}/read` - отметка о прочтении
- `POST /api/notifications/batch` - пакетная отправка

### Управление шаблонами

- `GET /api/templates` - получение списка шаблонов
- `POST /api/templates` - создание шаблона
- `PUT /api/templates/{id}` - обновление шаблона
- `DELETE /api/templates/{id}` - удаление шаблона

### Управление предпочтениями

- `GET /api/preferences/{userId}` - получение предпочтений
- `PUT /api/preferences/{userId}` - обновление предпочтений
- `POST /api/preferences/{userId}/subscribe` - управление подписками

### Аналитика и мониторинг

- `GET /api/analytics/statistics/overall` - общая статистика
- `GET /api/analytics/statistics/period` - статистика за период
- `GET /api/analytics/performance` - метрики производительности
- `GET /api/analytics/statistics/errors` - статистика ошибок

### Rate Limiting

- `GET /api/rate-limiting/user/{userId}` - статус лимитов пользователя
- `GET /api/rate-limiting/global` - глобальный статус
- `POST /api/rate-limiting/user/{userId}/blacklist` - черный список

## Конфигурация

### Основные параметры

```yaml
app:
  notification:
    # Использование очереди
    use-queue: true
    
    # Настройки retry
    retry:
      max-attempts: 3
      base-delay-minutes: 1
      exponential-backoff: true
    
    # Rate limiting
    rate-limit:
      user-per-minute: 10
      user-per-hour: 100
      user-per-day: 1000
      ip-per-minute: 20
      global-per-minute: 1000
    
    # Очередь
    queue:
      batch-size: 50
      processing-interval: 5000
      cleanup-days: 30
```

### Настройки каналов

```yaml
# Email
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}

# Firebase
firebase:
  project-id: ${FIREBASE_PROJECT_ID}
  private-key: ${FIREBASE_PRIVATE_KEY}

# Telegram
telegram:
  bot-token: ${TELEGRAM_BOT_TOKEN}
  webhook-url: ${TELEGRAM_WEBHOOK_URL}

# SMS
sms:
  provider: twilio
  account-sid: ${TWILIO_ACCOUNT_SID}
  auth-token: ${TWILIO_AUTH_TOKEN}
```

## Модели данных

### Notification

```java
@Entity
public class Notification {
    private String notificationId;
    private UUID userId;
    private NotificationType type;
    private NotificationCategory category;
    private NotificationPriority priority;
    private String subject;
    private String content;
    private String htmlContent;
    private NotificationStatus status;
    // ... другие поля
}
```

### NotificationQueue

```java
@Entity
public class NotificationQueue {
    private Long notificationId;
    private UUID userId;
    private String channelType;
    private NotificationPriority priority;
    private NotificationStatus status;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
    // ... другие поля
}
```

## Шаблоны

### Структура шаблона

```html
<!DOCTYPE html>
<html>
<head>
    <title>{{subject}}</title>
</head>
<body>
    <h1>Здравствуйте, {{username}}!</h1>
    <p>{{message}}</p>
    {{#if hasAction}}
    <a href="{{actionUrl}}">{{actionText}}</a>
    {{/if}}
</body>
</html>
```

### Мультиязычная поддержка

Шаблоны поддерживают мультиязычность через суффиксы:
- `welcome_ru.html` - русский
- `welcome_en.html` - английский
- `welcome.html` - шаблон по умолчанию

## Безопасность

### Аутентификация

Используется JWT токен для аутентификации запросов.

### Авторизация

- `USER` - базовый доступ к уведомлениям
- `ADMIN` - полный доступ ко всем функциям

### Rate Limiting

- Пользовательские лимиты: 10/мин, 100/час, 1000/день
- IP лимиты: 20/мин, 500/час
- Глобальные лимиты: 1000/мин, 10000/час

### Валидация контента

- Проверка HTML контента на XSS
- Валидация email адресов
- Проверка размера контента

## Мониторинг

### Health Checks

- `/actuator/health` - общий статус сервиса
- `/actuator/health/notification` - детальная информация

### Метрики

- Количество отправленных уведомлений
- Время доставки
- Процент ошибок
- Размер очереди
- Производительность каналов

### Логирование

Уровни логирования:
- `ERROR` - критические ошибки
- `WARN` - предупреждения и retry
- `INFO` - основная информация
- `DEBUG` - детальная отладка

## Тестирование

### Интеграционные тесты

```bash
# Запуск тестов
./gradlew test

# Интеграционные тесты
./gradlew integrationTest
```

### Тестовые данные

Тестовые данные генерируются автоматически через `TestDataFactory`.

## Развертывание

### Docker

```bash
# Сборка образа
docker build -t notification-service .

# Запуск
docker run -p 8086:8086 notification-service
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: notification-service
  template:
    metadata:
      labels:
        app: notification-service
    spec:
      containers:
      - name: notification-service
        image: notification-service:latest
        ports:
        - containerPort: 8086
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

## Производительность

### Оптимизация

- Асинхронная обработка уведомлений
- Пакетная отправка
- Кэширование шаблонов
- Connection pooling для внешних сервисов

### Масштабирование

- Горизонтальное масштабирование через реплики
- Разделение очередей по приоритетам
- Балансировка нагрузки между каналами

## Траблшутинг

### Частые проблемы

1. **Уведомления не отправляются**
   - Проверить статус очереди: `/api/analytics/statistics/overall`
   - Проверить конфигурацию каналов
   - Проверить логи ошибок

2. **Высокое время доставки**
   - Проверить размер очереди
   - Проверить производительность внешних сервисов
   - Проверить настройки retry

3. **Rate limiting ошибки**
   - Проверить лимиты пользователя: `/api/rate-limiting/user/{userId}`
   - Проверить глобальные лимиты
   - Проверить черный список

### Логирование проблем

```bash
# Просмотр логов
docker logs notification-service

# Фильтрация по ошибкам
docker logs notification-service | grep ERROR
```

## Версионирование

- Версия: 1.0.0
- Совместимость: Java 17+, Spring Boot 3.x
- База данных: PostgreSQL 14+

## Контакты

- Разработчик: DN Quest Team
- Документация: [Wiki](https://github.com/dn-quest/notification-service/wiki)
- Issues: [GitHub Issues](https://github.com/dn-quest/notification-service/issues)