# DN Quest Frontend

Frontend приложение для DN Quest Engine, построенное на Vue.js 3 с Vite. Поддерживает работу с микросервисной архитектурой через API Gateway.

## Технологии

- Vue.js 3
- Vite
- Naive UI
- Tailwind CSS
- Vue Router
- Axios
- Vitest (для тестирования)
- WebSocket (для real-time функциональности)

## Микросервисная архитектура

Фронтенд интегрирован со следующими микросервисами через API Gateway:

- **Authentication Service** (`/api/auth/*`) - Аутентификация и управление пользователями
- **User Management Service** (`/api/users/*`) - Управление профилями пользователей
- **Quest Management Service** (`/api/quests/*`) - Управление квестами
- **Game Engine Service** (`/api/game/*`) - Игровая логика и сессии
- **Team Management Service** (`/api/teams/*`) - Управление командами
- **File Storage Service** (`/api/files/*`) - Загрузка и хранение файлов
- **Notification Service** (`/api/notifications/*`) - Уведомления
- **Statistics Service** (`/api/statistics/*`) - Статистика и аналитика

## Разработка

### Установка зависимостей

```bash
npm install
```

### Переменные окружения

Скопируйте `.env.example` в `.env.development` и настройте переменные:

```bash
cp .env.example .env.development
```

Основные переменные:
- `VITE_API_BASE_URL` - URL API Gateway
- `VITE_WS_BASE_URL` - URL WebSocket Gateway
- `VITE_ENABLE_WEBSOCKET` - Включение WebSocket функциональности
- `VITE_ENABLE_FILE_UPLOAD` - Включение загрузки файлов
- `VITE_DEBUG` - Режим отладки

### Запуск dev сервера

```bash
npm run dev
```

### Сборка для продакшена

```bash
npm run build
```

### Предпросмотр сборки

```bash
npm run preview
```

### Тестирование

```bash
# Запуск тестов
npm run test

# Запуск тестов с UI
npm run test:ui

# Запуск тестов с покрытием кода
npm run test:coverage

# Запуск тестов в CI режиме
npm run test:run
```

## Структура проекта

```
src/
├── components/         # Vue компоненты
│   ├── FileUpload.vue # Компонент загрузки файлов
│   ├── AppHeader.vue  # Шапка приложения
│   └── GameHeader.vue # Шапка игрового режима
├── pages/             # Страницы приложения
│   ├── Home.vue       # Главная страница
│   ├── Play.vue       # Игровая страница
│   ├── Register.vue   # Регистрация
│   └── QuestForm.vue  # Форма создания/редактирования квеста
├── services/          # API сервисы
│   ├── api.js         # Основной API клиент с поддержкой микросервисов
│   ├── auth.js        # Сервис аутентификации с JWT
│   ├── websocket.js   # WebSocket сервис для real-time
│   └── errorHandler.js # Централизованная обработка ошибок
├── tests/             # Тесты
│   ├── api.test.js    # Тесты API сервисов
│   ├── auth.test.js   # Тесты аутентификации
│   └── setup.js       # Настройка тестового окружения
├── utils/             # Утилиты
├── router/            # Роутинг
├── App.vue            # Главный компонент
└── main.js            # Точка входа
```

## API и микросервисы

### Аутентификация

Фронтенд использует JWT токены с автоматическим обновлением:

```javascript
import { login, logout, fetchCurrentUser } from '@/services/auth';

// Вход
const { user } = await login({ username, password });

// Получение текущего пользователя
const user = await fetchCurrentUser();

// Выход
await logout();
```

### API сервисы

Каждый микросервис имеет свой собственный сервис:

```javascript
import { authService, questService, gameService } from '@/services/api';

// Работа с квестами
const quests = await questService.getQuests();
const quest = await questService.getQuestById(id);

// Игровая логика
const currentLevel = await gameService.getCurrentLevel(sessionId);
await gameService.submitCode(sessionId, { rawCode: 'CODE123', userId });
```

### WebSocket

Real-time функциональность через WebSocket:

```javascript
import { useGameEvents } from '@/services/websocket';

const { unsubscribeAll } = useGameEvents({
  onCodeSubmitted: (data) => {
    console.log('Код отправлен:', data);
  },
  onLevelCompleted: (data) => {
    console.log('Уровень завершен:', data);
  }
});
```

### Загрузка файлов

Компонент для загрузки файлов с поддержкой File Storage Service:

```vue
<template>
  <FileUpload
    v-model="files"
    :max-files="5"
    :max-file-size="10485760"
    accepted-types="image/*,.pdf"
    @upload-success="handleUploadSuccess"
  />
</template>
```

## Обработка ошибок

Централизованная обработка ошибок с пользовательскими сообщениями:

```javascript
import { handleError } from '@/services/errorHandler';

try {
  await someApiCall();
} catch (error) {
  handleError(error, {
    context: 'User registration',
    customMessage: 'Не удалось зарегистрировать пользователя'
  });
}
```

## Кэширование

API клиент поддерживает кэширование GET запросов:

```javascript
import { apiUtils } from '@/services/api';

// Очистка кэша
apiUtils.clearCache();

// Очистка кэша по шаблону
apiUtils.clearCache('quests');
```

## Конфигурация окружений

- `.env.development` - Настройки для разработки
- `.env.production` - Настройки для продакшена
- `.env.test` - Настройки для тестирования

## Docker

Фронтенд может быть запущен в Docker контейнере:

```bash
docker build -t dn-quest-frontend .
docker run -p 5173:5173 dn-quest-frontend
```

## Поддержка браузеров

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## Вклад в разработку

1. Fork проекта
2. Создайте feature ветку
3. Внесите изменения
4. Добавьте тесты
5. Убедитесь что все тесты проходят
6. Создайте Pull Request

## Лицензия

MIT License
