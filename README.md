# DN Quest - Платформа для онлайн квестов

## Обзор

DN Quest - это современная веб-платформа для создания и прохождения онлайн квестов, поддерживающая как одиночное, так и командное прохождение. Платформа построена на микросервисной архитектуре с использованием современных технологий.

## Технологический стек

### Backend
- **Java 21** - последняя версия Java
- **Spring Boot 3.x** - фреймворк для создания микросервисов
- **Spring Security** - безопасность и аутентификация
- **Spring Data JPA** - работа с базой данных
- **PostgreSQL** - основная база данных
- **Redis** - кэширование и сессии
- **Kafka** - асинхронная коммуникация между сервисами

### Frontend
- **Vue 3** - современный JavaScript фреймворк
- **Vite** - быстрый сборщик
- **Naive UI** - компонентная библиотека
- **Tailwind CSS** - утилитарный CSS фреймворк
- **Axios** - HTTP клиент

### Инфраструктура
- **Docker** - контейнеризация
- **Docker Compose** - оркестрация контейнеров
- **Kubernetes** - оркестрация в продакшене
- **Nginx** - веб-сервер и балансировщик
- **Prometheus + Grafana** - мониторинг
- **ELK Stack** - логирование

## Архитектура

### Микросервисы

1. **API Gateway** - шлюз для маршрутизации запросов
2. **Authentication Service** - аутентификация и управление пользователями
3. **Quest Management Service** - управление квестами
4. **Game Engine Service** - игровой движок
5. **Team Management Service** - управление командами
6. **Notification Service** - уведомления
7. **Statistics Service** - статистика и аналитика
8. **File Storage Service** - хранение файлов

### База данных

- **PostgreSQL** - основные данные
- **Redis** - кэш и сессии
- **ClickHouse** - аналитические данные

## Быстрый старт

### Требования

- Java 21+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 14+

### Установка и запуск

1. **Клонирование репозитория**
```bash
git clone https://github.com/your-org/dn-quest.git
cd dn-quest
```

2. **Запуск инфраструктуры**
```bash
# Запуск базы данных и Redis
docker-compose up -d postgres redis

# Запуск Kafka (опционально)
docker-compose -f docker-compose.kafka.yml up -d
```

3. **Настройка базы данных**
```bash
# Создание базы данных
createdb dnqdb

# Применение миграций
./gradlew flywayMigrate
```

4. **Запуск backend**
```bash
./gradlew bootRun
```

5. **Запуск frontend**
```bash
cd frontend
npm install
npm run dev
```

6. **Сборка frontend для продакшена**
```bash
cd frontend
npm run build
```

### Docker запуск

```bash
# Полный запуск всех сервисов
docker-compose up -d

# Только основные сервисы
docker-compose up -d postgres redis backend frontend
```

## Структура проекта

```
dn-quest/
├── src/main/java/dn/quest/          # Backend код
│   ├── controllers/                 # REST контроллеры
│   ├── services/                    # Бизнес-логика
│   ├── repositories/                # Доступ к данным
│   ├── model/                       # Модели данных
│   ├── config/                      # Конфигурация
│   └── exceptions/                  # Обработка ошибок
├── frontend/                        # Frontend код
│   ├── src/
│   │   ├── components/              # Vue компоненты
│   │   ├── pages/                   # Страницы
│   │   ├── services/                # API сервисы
│   │   └── router/                  # Роутинг
│   ├── public/                      # Статические файлы
│   └── dist/                        # Собранный frontend
├── microservices/                   # Микросервисы
│   ├── authentication-service/       # Сервис аутентификации
│   ├── quest-service/               # Сервис квестов
│   └── game-engine-service/         # Игровой движок
├── docs/                           # Документация
├── docker-compose.yml              # Docker конфигурация
├── docker-compose.kafka.yml        # Kafka конфигурация
└── Makefile                        # Сборочные скрипты
```

## Основные функции

### Для игроков
- Регистрация и аутентификация
- Просмотр доступных квестов
- Прохождение квестов в одиночку или командой
- Ввод кодов и получение подсказок
- Отслеживание прогресса

### Для авторов
- Создание и редактирование квестов
- Настройка уровней, кодов и подсказок
- Управление публикацией квестов
- Мониторинг прохождения

### Для администраторов
- Управление пользователями
- Мониторинг системы
- Аналитика и статистика
- Управление ролями

## API документация

### Основные эндпоинты

#### Аутентификация
- `POST /api/auth/register` - Регистрация
- `POST /api/auth/login` - Вход
- `POST /api/auth/refresh` - Обновление токена

#### Квесты
- `GET /api/quests` - Список квестов
- `POST /api/quests` - Создание квеста
- `GET /api/quests/{id}` - Получение квеста
- `PUT /api/quests/{id}` - Обновление квеста

#### Игровые сессии
- `POST /api/game/sessions` - Начать игру
- `POST /api/game/sessions/{id}/submit-code` - Ввести код
- `GET /api/game/sessions/{id}/current-level` - Текущий уровень

#### Команды
- `GET /api/teams` - Список команд
- `POST /api/teams` - Создать команду
- `POST /api/teams/{id}/invite` - Пригласить в команду

Полная документация доступна по адресу: `http://localhost:8080/swagger-ui.html`

## Тестирование

### Запуск тестов

```bash
# Backend тесты
./gradlew test

# Frontend тесты
cd frontend
npm run test

# Интеграционные тесты
./gradlew integrationTest
```

### Тестовое покрытие

- Backend: 85%+
- Frontend: 80%+

## Разработка

### Локальная разработка

1. **Настройка окружения**
```bash
# Переменные окружения
export DATABASE_URL=jdbc:postgresql://localhost:5432/dnqdb
export JWT_SECRET=your-secret-key
export REDIS_HOST=localhost
```

2. **Запуск в режиме разработки**
```bash
# Backend с hot reload
./gradlew bootRun --args='--spring.profiles.active=dev'

# Frontend с hot reload
cd frontend
npm run dev
```

### Код стиль

- Java: Google Java Style Guide
- JavaScript: ESLint + Prettier
- Используйте pre-commit hooks для проверки

### Конвенции коммитов

```
feat: добавление новой функции
fix: исправление бага
docs: обновление документации
style: форматирование кода
refactor: рефакторинг
test: добавление тестов
chore: обновление зависимостей
```

## Развертывание

### Продакшен

1. **Сборка образов**
```bash
./gradlew jibDockerBuild
cd frontend && npm run build
```

2. **Развертывание в Kubernetes**
```bash
kubectl apply -f k8s/
```

3. **Мониторинг**
- Grafana: `http://your-domain/grafana`
- Prometheus: `http://your-domain/prometheus`
- Kibana: `http://your-domain/kibana`

### Переменные окружения

```bash
# База данных
DATABASE_URL=jdbc:postgresql://localhost:5432/dnqdb
DATABASE_USERNAME=dn
DATABASE_PASSWORD=your-password

# JWT
JWT_SECRET=your-super-secret-key
JWT_EXPIRATION_MS=86400000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## Мониторинг и логирование

### Метрики
- JVM метрики
- HTTP запросы
- База данных
- Kafka

### Логи
- Структурированные логи
- Correlation ID для трейсинга
- Уровни логирования

### Алерты
- Высокая нагрузка
- Ошибки базы данных
- Недоступность сервисов

## Безопасность

### Аутентификация
- JWT токены
- Refresh токены
- Rate limiting

### Авторизация
- Ролевая модель
- RBAC
- API ключи

### Защита
- HTTPS
- CORS
- SQL Injection защита
- XSS защита

## Производительность

### Оптимизация
- Кэширование
- Индексы базы данных
- Connection pooling
- Асинхронная обработка

### Масштабирование
- Горизонтальное масштабирование
- Load balancing
- Auto-scaling

## Вклад в проект

1. Fork репозитория
2. Создайте feature branch
3. Вносите изменения
4. Добавьте тесты
5. Создайте Pull Request

## Лицензия

MIT License - см. файл [LICENSE](LICENSE)

## Поддержка

- Документация: [docs/](docs/)
- Issues: [GitHub Issues](https://github.com/your-org/dn-quest/issues)
- Дискорд: [ссылка на Discord]

## Дорожная карта

### v1.0 (Текущая версия)
- [x] Базовая функциональность
- [x] Аутентификация
- [x] Создание квестов
- [x] Игровой движок

### v1.1 (Планируется)
- [ ] Мобильное приложение
- [ ] Расширенная аналитика
- [ ] Турниры
- [ ] Интеграция с мессенджерами

### v2.0 (Будущее)
- [ ] ИИ для генерации квестов
- [ ] VR/AR поддержка
- [ ] Голосовые квесты
- [ ] Международные квесты

## Авторы

- [Ваше имя](https://github.com/your-username) - Lead Developer
- [Имя команды](https://github.com/team) - Development Team

## Благодарности

- Сообществу Spring Boot
- Команде Vue.js
- Всем тестировщикам и контрибьюторам