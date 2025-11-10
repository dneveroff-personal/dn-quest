# Team Management Service

Микросервис для управления командами в системе DN Quest. Обеспечивает полную функциональность для создания, управления и администрирования команд, участников и приглашений.

## Функциональность

### Управление командами
- Создание, обновление и удаление команд
- Управление составом команды
- Передача прав капитана
- Настройки приватности и лимитов
- Статистика и аналитика команд

### Управление участниками
- Добавление и удаление участников
- Управление ролями (CAPTAIN, MODERATOR, MEMBER)
- История участия в командах
- Статистика активности участников

### Приглашения
- Отправка приглашений в команды
- Принятие и отклонение приглашений
- Отзыв приглашений
- Управление сроками действия
- Массовые операции с приглашениями

### Интеграция с другими сервисами
- **Authentication Service**: валидация токенов и аутентификация
- **User Management Service**: информация о пользователях
- **Game Engine Service**: игровые сессии и статистика
- **Statistics Service**: агрегация и аналитика данных
- **Notification Service**: уведомления о событиях

## Технологический стек

- **Java 17** с Spring Boot 3.2.0
- **PostgreSQL** с Flyway миграциями
- **Redis** для кэширования
- **Apache Kafka** для событий
- **Spring Cloud OpenFeign** для интеграции
- **Docker** для контейнеризации

## API Эндпоинты

### Команды
```
GET    /api/teams                    - Получение списка команд
POST   /api/teams                    - Создание команды
GET    /api/teams/{id}               - Получение информации о команде
PUT    /api/teams/{id}               - Обновление команды
DELETE /api/teams/{id}               - Удаление команды
GET    /api/teams/{id}/members       - Получение участников команды
POST   /api/teams/{id}/members       - Добавление участника
DELETE /api/teams/{id}/members/{userId} - Удаление участника
PUT    /api/teams/{id}/members/{userId}/role - Изменение роли участника
PUT    /api/teams/{id}/captain/{userId} - Передача капитанства
GET    /api/teams/{id}/settings      - Получение настроек команды
PUT    /api/teams/{id}/settings      - Обновление настроек команды
GET    /api/teams/{id}/statistics    - Получение статистики команды
```

### Приглашения
```
GET    /api/teams/{teamId}/invitations          - Получение приглашений команды
POST   /api/teams/{teamId}/invitations          - Отправка приглашения
PUT    /api/invitations/{id}/respond            - Ответ на приглашение
PUT    /api/invitations/{id}/accept             - Принятие приглашения
PUT    /api/invitations/{id}/decline            - Отклонение приглашения
DELETE /api/invitations/{id}                    - Отзыв приглашения
```

### Пользователи
```
GET    /api/users/{id}                          - Получение информации о пользователе
GET    /api/users/{id}/teams                    - Получение команд пользователя
GET    /api/users/{id}/teams/active             - Активные команды пользователя
GET    /api/users/{id}/teams/captain            - Команды где пользователь капитан
GET    /api/users/current                       - Текущий пользователь
GET    /api/users/current/teams                 - Команды текущего пользователя
```

## События Kafka

### Team Events
- `team.created` - создание команды
- `team.updated` - обновление команды
- `team.deleted` - удаление команды
- `team.member.added` - добавление участника
- `team.member.removed` - удаление участника
- `team.member.role_changed` - изменение роли участника
- `team.captain.changed` - передача капитанства
- `team.settings.updated` - обновление настроек

### Invitation Events
- `team.invitation.sent` - отправка приглашения
- `team.invitation.accepted` - принятие приглашения
- `team.invitation.declined` - отклонение приглашения
- `team.invitation.revoked` - отзыв приглашения

## Конфигурация

### Переменные окружения
```bash
# База данных
DB_HOST=localhost
DB_PORT=5432
DB_NAME=team_management_db
DB_USERNAME=team_management_user
DB_PASSWORD=team_management_pass

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Сервисы
AUTHENTICATION_SERVICE_URL=http://localhost:8081
USER_MANAGEMENT_SERVICE_URL=http://localhost:8082
GAME_ENGINE_SERVICE_URL=http://localhost:8084
STATISTICS_SERVICE_URL=http://localhost:8085
NOTIFICATION_SERVICE_URL=http://localhost:8086

# Безопасность
JWT_SECRET=team-management-secret-key
JWT_EXPIRATION=86400000

# Лимиты
MAX_TEAMS_PER_USER=5
MAX_MEMBERS_PER_TEAM=10
MAX_INVITATIONS_PER_TEAM=20
MAX_DAILY_INVITATIONS_PER_USER=10
INVITATION_EXPIRATION_HOURS=72
```

## Запуск

### Локальная разработка
```bash
# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun

# С Docker
docker-compose up -d
```

### Docker
```bash
# Сборка образа
docker build -t dn-quest/team-management-service:latest .

# Запуск контейнера
docker run -p 8083:8083 \
  -e DB_HOST=localhost \
  -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  dn-quest/team-management-service:latest
```

## Тестирование

### Unit тесты
```bash
./gradlew test
```

### Integration тесты
```bash
./gradlew integrationTest
```

### Тесты с Testcontainers
```bash
./gradlew test -Dspring.profiles.active=test
```

## Мониторинг

### Health Check
```bash
curl http://localhost:8083/api/actuator/health
```

### Метрики
```bash
curl http://localhost:8083/api/actuator/metrics
curl http://localhost:8083/api/actuator/prometheus
```

## Кэширование

Сервис использует Redis для кэширования следующих данных:
- Информация о командах (5 минут)
- Информация о пользователях (10 минут)
- Приглашения (3 минуты)
- Состав команд (4 минуты)
- Статистика команд (6 минут)

## Безопасность

- JWT токены для аутентификации
- Проверка прав доступа к операциям
- Валидация бизнес-правил
- Ограничения на количество ресурсов

## Производительность

- Оптимизированные запросы к базе данных
- Индексы для быстрого поиска
- Кэширование часто используемых данных
- Асинхронная обработка событий
- Пул соединений с базой данных

## Логирование

Уровни логирования:
- `dn.quest.teammanagement`: INFO
- `org.springframework.kafka`: WARN
- `org.hibernate.SQL`: WARN

Файлы логов:
- Консоль: стандартный вывод
- Файл: `logs/team-management-service.log`

## Разработка

### Структура проекта
```
src/main/java/dn/quest/teammanagement/
├── config/          # Конфигурация
├── controller/      # REST контроллеры
├── service/         # Бизнес-логика
├── repository/      # Доступ к данным
├── entity/          # Сущности JPA
├── dto/             # DTO классы
├── mapper/          # Мапперы
├── client/          # Feign клиенты
├── event/           # События Kafka
├── exception/       # Исключения
└── enums/           # Перечисления
```

### Правила разработки
- Использовать Java 17 и Spring Boot 3.2.0
- Следовать принципам Clean Code
- Писать unit и integration тесты
- Использовать Lombok для уменьшения шаблонного кода
- Валидировать все входные данные
- Обрабатывать все исключения
- Логировать важные операции