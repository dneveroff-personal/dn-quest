# File Storage Service

Микросервис для хранения и управления файлами в архитектуре DN Quest.

## Обзор

File Storage Service предоставляет централизованное решение для хранения файлов с поддержкой различных типов хранилищ, валидации, обработки изображений и генерации предписанных URL.

## Основные возможности

### 🗂️ Управление файлами
- Загрузка одиночных и пакетных файлов
- Скачивание файлов с отслеживанием статистики
- Удаление файлов с очисткой хранилища
- Поиск файлов по различным критериям
- Метаданные файлов с расширенной информацией

### 🏗️ Множественные хранилища
- **Локальное хранилище** - для разработки и тестирования
- **MinIO** - S3-совместимое хранилище для продакшена
- **AWS S3** - облачное хранилище (в планах)
- **CDN интеграция** - для быстрой доставки контента (в планах)

### 🔒 Безопасность и валидация
- Валидация типов файлов и размеров
- Контроль доступа на основе владельца и публичности
- JWT аутентификация через Authentication Service
- Генерация временных предписанных URL

### 🖼️ Обработка изображений
- Сжатие изображений с настраиваемым качеством
- Генерация миниатюр (thumbnails)
- Изменение размера, обрезка, поворот
- Добавление водяных знаков
- Конвертация между форматами

### 📊 Мониторинг и статистика
- Статистика использования хранилища
- Метрики загрузки/скачивания
- Автоматическая очистка временных файлов
- Health checks для всех хранилищ

## API Эндпоинты

### Основные операции

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | `/api/files/upload` | Загрузить файл |
| POST | `/api/files/batch-upload` | Пакетная загрузка |
| GET | `/api/files/{fileId}` | Получить метаданные |
| GET | `/api/files/{fileId}/download` | Скачать файл |
| DELETE | `/api/files/{fileId}` | Удалить файл |
| POST | `/api/files/search` | Поиск файлов |

### URL и доступ

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/api/files/{fileId}/presigned-url` | Предписанный URL для скачивания |
| POST | `/api/files/presigned-upload-url` | Предписанный URL для загрузки |
| GET | `/api/files/public` | Публичные файлы |
| GET | `/api/files/my-files` | Файлы пользователя |

### Статистика

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/api/files/statistics` | Статистика хранилища (админ) |
| GET | `/api/files/my-statistics` | Статистика пользователя |

## Типы файлов

- **AVATAR** - Аватары пользователей (макс. 5MB)
- **QUEST_MEDIA** - Медиа файлы квестов (макс. 20MB)
- **LEVEL_FILE** - Файлы уровней (макс. 50MB)
- **TEMPORARY** - Временные файлы
- **DOCUMENT** - Документы (макс. 50MB)
- **IMAGE** - Изображения (макс. 20MB)
- **VIDEO** - Видео (макс. 500MB)
- **AUDIO** - Аудио (макс. 100MB)

## Конфигурация

### Основные параметры

```yaml
server:
  port: 8088
  servlet:
    context-path: /api/files

spring:
  application:
    name: file-storage-service
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/dnquest_files}
    username: ${DATABASE_USERNAME:dn}
    password: ${DATABASE_PASSWORD:dn}

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

### MinIO конфигурация

```yaml
minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket-name: ${MINIO_BUCKET_NAME:dn-quest-files}
```

### Конфигурация файлов

```yaml
file:
  upload:
    max-size: 50MB
    allowed-types: image/*,video/*,audio/*,application/pdf
    allowed-extensions: jpg,jpeg,png,gif,mp4,mp3,pdf
  
  storage:
    default-type: LOCAL
    local:
      path: ./uploads
      temp-path: ./temp
      max-size: 10GB
```

## Архитектура

### Стратегия хранения

Сервис использует паттерн Strategy для поддержки различных типов хранилищ:

```
StorageStrategy (интерфейс)
├── LocalStorageStrategy
├── MinioStorageStrategy
├── S3StorageStrategy (в планах)
└── CDNStorageStrategy (в планах)
```

### Поток обработки файла

1. **Валидация** - проверка типа, размера, расширения
2. **Выбор хранилища** - на основе типа файла и конфигурации
3. **Обработка изображения** - сжатие, генерация миниатюр
4. **Сохранение** - в выбранное хранилище
5. **Метаданные** - сохранение в базу данных
6. **Ответ** - возврат информации о файле

## Развертывание

### Локальная разработка

```bash
# Запуск PostgreSQL
docker run -d --name postgres-files \
  -e POSTGRES_DB=dnquest_files \
  -e POSTGRES_USER=dn \
  -e POSTGRES_PASSWORD=dn \
  -p 5432:5432 postgres:15

# Запуск MinIO
docker run -d --name minio \
  -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  minio/minio server /data --console-address ":9001"

# Запуск сервиса
./gradlew bootRun
```

### Docker

```bash
# Сборка образа
./gradlew jibDockerBuild

# Запуск
docker run -d --name file-storage-service \
  -p 8088:8088 \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/dnquest_files \
  -e DATABASE_USERNAME=dn \
  -e DATABASE_PASSWORD=dn \
  -e MINIO_ENDPOINT=http://minio:9000 \
  dn-quest/file-storage-service:1.0.0
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: file-storage-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: file-storage-service
  template:
    metadata:
      labels:
        app: file-storage-service
    spec:
      containers:
      - name: file-storage-service
        image: dn-quest/file-storage-service:1.0.0
        ports:
        - containerPort: 8088
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
        - name: MINIO_ENDPOINT
          value: "http://minio:9000"
```

## Мониторинг

### Health Checks

- `/actuator/health` - Общее состояние сервиса
- `/actuator/health/storage` - Состояние хранилищ
- `/actuator/metrics` - Метрики производительности

### Логирование

```yaml
logging:
  level:
    dn.quest.filestorage: INFO
    io.minio: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Метрики

- Количество загруженных/скачанных файлов
- Размер хранилища по типам
- Время обработки файлов
- Ошибки по типам

## Безопасность

### Аутентификация

Сервис использует JWT токены для аутентификации через Authentication Service.

### Авторизация

- Пользователи имеют доступ только к своим файлам
- Публичные файлы доступны всем
- Администраторы имеют доступ ко всем файлам

### Валидация

- Проверка типов файлов (whitelist)
- Ограничение размеров файлов
- Сканирование имен файлов на недопустимые символы

## Тестирование

### Unit тесты

```bash
./gradlew test
```

### Интеграционные тесты

```bash
./gradlew integrationTest
```

### Тестирование производительности

```bash
./gradlew gatlingRun
```

## Траблшутинг

### Частые проблемы

1. **Файл не загружается**
   - Проверьте размер файла
   - Проверьте тип файла
   - Проверьте доступность хранилища

2. **Ошибка доступа к файлу**
   - Проверьте JWT токен
   - Убедитесь что файл ваш или публичный

3. **Медленная загрузка**
   - Проверьте состояние хранилища
   - Проверьте сетевое подключение
   - Рассмотрите сжатие изображений

### Логи

```bash
# Просмотр логов
docker logs file-storage-service

# Логи с отладкой
docker logs file-storage-service | grep "DEBUG"
```

## Дорожная карта

### В планах:

- [ ] AWS S3 интеграция
- [ ] CDN интеграция (CloudFlare)
- [ ] Вирусное сканирование файлов
- [ ] Расширенная аналитика
- [ ] Версионирование файлов
- [ ] Резервное копирование
- [ ] Кэширование метаданных
- [ ] GraphQL API

## Вклад

1. Fork проекта
2. Создайте feature branch
3. Внесите изменения
4. Отправьте pull request

## Лицензия

MIT License