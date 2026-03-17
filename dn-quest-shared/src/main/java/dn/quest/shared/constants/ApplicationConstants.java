package dn.quest.shared.constants;

/**
 * Константы приложения DN Quest
 */
public final class ApplicationConstants {
    
    private ApplicationConstants() {
        // Утилитарный класс
    }
    
    /**
     * Константы безопасности
     */
    public static final class Security {
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String USER_ID_HEADER = "X-User-Id";
        public static final String USER_ROLE_HEADER = "X-User-Role";
        public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
        
        private Security() {}
    }
    
    /**
     * Константы API
     */
    public static final class Api {
        public static final String API_PREFIX = "/api";
        public static final String V1_PREFIX = "/v1";
        public static final String BASE_PATH = API_PREFIX + V1_PREFIX;
        
        // Параметры пагинации
        public static final String PAGE_PARAM = "page";
        public static final String SIZE_PARAM = "size";
        public static final String SORT_PARAM = "sort";
        public static final String DEFAULT_PAGE = "0";
        public static final String DEFAULT_SIZE = "20";
        public static final int MAX_PAGE_SIZE = 100;
        
        // Параметры поиска
        public static final String SEARCH_PARAM = "search";
        public static final String FILTER_PARAM = "filter";
        
        private Api() {}
    }
    
    /**
     * Константы квестов
     */
    public static final class Quest {
        public static final int MIN_TITLE_LENGTH = 3;
        public static final int MAX_TITLE_LENGTH = 300;
        public static final int MAX_DESCRIPTION_LENGTH = 10000;
        public static final int MAX_LEVELS_COUNT = 100;
        public static final int MIN_LEVELS_COUNT = 1;
        public static final int MAX_CODE_LENGTH = 200;
        public static final int MAX_HINT_LENGTH = 1000;
        public static final int MAX_HINTS_PER_LEVEL = 10;
        
        private Quest() {}
    }
    
    /**
     * Константы пользователей
     */
    public static final class User {
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 64;
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 128;
        public static final int MAX_EMAIL_LENGTH = 255;
        public static final int MAX_PUBLIC_NAME_LENGTH = 128;
        public static final int MAX_BIO_LENGTH = 500;
        
        private User() {}
    }
    
    /**
     * Константы команд
     */
    public static final class Team {
        public static final int MIN_TEAM_NAME_LENGTH = 3;
        public static final int MAX_TEAM_NAME_LENGTH = 120;
        public static final int MAX_TEAM_MEMBERS = 10;
        public static final int MIN_TEAM_MEMBERS = 1;
        
        private Team() {}
    }
    
    /**
     * Константы игровых сессий
     */
    public static final class Game {
        public static final int MAX_ATTEMPTS_PER_MINUTE = 10;
        public static final int MAX_ATTEMPTS_PER_HOUR = 100;
        public static final int MAX_ATTEMPTS_PER_DAY = 1000;
        public static final int CODE_ATTEMPT_TIMEOUT_SECONDS = 1;
        public static final int MAX_SESSION_DURATION_HOURS = 24;
        
        private Game() {}
    }
    
    /**
     * Константы кэширования
     */
    public static final class Cache {
        public static final String USERS_CACHE = "users";
        public static final String QUESTS_CACHE = "quests";
        public static final String TEAMS_CACHE = "teams";
        public static final String SESSIONS_CACHE = "sessions";
        public static final String CODES_CACHE = "codes";
        
        public static final long DEFAULT_CACHE_TTL = 1800; // 30 минут
        public static final long SHORT_CACHE_TTL = 300; // 5 минут
        public static final long LONG_CACHE_TTL = 3600; // 1 час
        
        private Cache() {}
    }
    
    /**
     * Константы Kafka
     */
    public static final class Kafka {
        // Топики
        public static final String USER_EVENTS_TOPIC = "user-events";
        public static final String QUEST_EVENTS_TOPIC = "quest-events";
        public static final String GAME_EVENTS_TOPIC = "game-events";
        public static final String TEAM_EVENTS_TOPIC = "team-events";
        public static final String NOTIFICATION_EVENTS_TOPIC = "notification-events";
        
        // Группы потребителей
        public static final String STATISTICS_GROUP = "statistics-service";
        public static final String NOTIFICATION_GROUP = "notification-service";
        
        private Kafka() {}
    }
    
    /**
     * Константы файлов
     */
    public static final class File {
        public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
        public static final long MAX_QUEST_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
        
        public static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        public static final String[] ALLOWED_FILE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf", ".txt", ".doc", ".docx"};
        
        private File() {}
    }
    
    /**
     * Константы валидации
     */
    public static final class Validation {
        public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{3,64}$";
        public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        public static final String TEAM_NAME_PATTERN = "^[a-zA-Z0-9\\s\\-_]{3,120}$";
        public static final String QUEST_TITLE_PATTERN = "^[\\p{L}\\p{N}\\s\\-_.,!?()]{3,300}$";
        
        private Validation() {}
    }
    
    /**
     * Константы HTTP статусов
     */
    public static final class HttpStatus {
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int CONFLICT = 409;
        public static final int UNPROCESSABLE_ENTITY = 422;
        public static final int TOO_MANY_REQUESTS = 429;
        public static final int INTERNAL_SERVER_ERROR = 500;
        
        private HttpStatus() {}
    }
    
    /**
     * Константы ошибок
     */
    public static final class ErrorCodes {
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
        public static final String AUTHORIZATION_ERROR = "AUTHORIZATION_ERROR";
        public static final String NOT_FOUND_ERROR = "NOT_FOUND_ERROR";
        public static final String CONFLICT_ERROR = "CONFLICT_ERROR";
        public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        public static final String RATE_LIMIT_ERROR = "RATE_LIMIT_ERROR";
        
        private ErrorCodes() {}
    }
}