package dn.quest.config;

/**
 * Константы приложения
 */
public final class ApplicationConstants {
    
    private ApplicationConstants() {
        // Приватный конструктор для предотвращения создания экземпляра
    }
    
    // JWT константы
    public static final String JWT_SECRET_PROPERTY = "JWT_SECRET";
    public static final String JWT_EXPIRATION_PROPERTY = "JWT_EXPIRATION_MS";
    public static final long DEFAULT_JWT_EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 часа
    
    // База данных константы
    public static final String DEFAULT_DB_USERNAME = "dn";
    public static final String DEFAULT_DB_PASSWORD = "dn";
    public static final String DEFAULT_DB_NAME = "dnqdb";
    
    // Административные константы
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";
    public static final String DEFAULT_ADMIN_PUBLIC_NAME = "Administrator";
    
    // Валидация константы
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MAX_PUBLIC_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_TEAM_NAME_LENGTH = 120;
    public static final int MAX_QUEST_TITLE_LENGTH = 300;
    public static final int MAX_CODE_VALUE_LENGTH = 200;
    
    // Игровые константы
    public static final int MAX_LEVELS_PER_QUEST = 100;
    public static final int MAX_CODES_PER_LEVEL = 50;
    public static final int MAX_HINTS_PER_LEVEL = 20;
    public static final int MAX_ATTEMPTS_PER_SESSION = 1000;
    public static final int DEFAULT_AUTO_PASS_TIME_SECONDS = 3600; // 1 час
    
    // Форматирование константы
    public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DURATION_FORMAT = "%02d:%02d:%02d";
    
    // Регулярные выражения
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]+$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    // HTTP константы
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    // API пути
    public static final String API_PREFIX = "/api";
    public static final String AUTH_PATH = "/api/auth";
    public static final String USERS_PATH = "/api/users";
    public static final String QUESTS_PATH = "/api/quests";
    public static final String TEAMS_PATH = "/api/teams";
    public static final String SESSIONS_PATH = "/api/sessions";
    
    // Сообщения об ошибках
    public static final String USER_NOT_FOUND = "Пользователь не найден";
    public static final String QUEST_NOT_FOUND = "Квест не найден";
    public static final String TEAM_NOT_FOUND = "Команда не найдена";
    public static final String SESSION_NOT_FOUND = "Сессия не найдена";
    public static final String LEVEL_NOT_FOUND = "Уровень не найден";
    public static final String INVALID_CREDENTIALS = "Неправильное имя пользователя или пароль";
    public static final String ACCESS_DENIED = "Доступ запрещен";
    public static final String ALREADY_EXISTS = "Уже существует";
    public static final String VALIDATION_FAILED = "Ошибка валидации";
    
    // Успешные сообщения
    public static final String USER_CREATED = "Пользователь успешно создан";
    public static final String QUEST_CREATED = "Квест успешно создан";
    public static final String QUEST_UPDATED = "Квест успешно обновлен";
    public static final String QUEST_DELETED = "Квест успешно удален";
    public static final String TEAM_CREATED = "Команда успешно создана";
    public static final String CODE_ACCEPTED = "Код принят";
    public static final String LEVEL_COMPLETED = "Уровень пройден";
    
    // Лимиты и пагинация
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_ATTEMPTS_LIMIT = 25;
    public static final int MAX_ATTEMPTS_LIMIT = 100;
    
    // Кэш константы
    public static final String USERS_CACHE = "users";
    public static final String QUESTS_CACHE = "quests";
    public static final String TEAMS_CACHE = "teams";
    public static final long DEFAULT_CACHE_TTL_MINUTES = 10;
    
    // Telegram константы
    public static final String TELEGRAM_BOT_USERNAME = "DN_QuestBot";
    public static final String TELEGRAM_BOT_TOKEN_PROPERTY = "BOT_TOKEN";
}