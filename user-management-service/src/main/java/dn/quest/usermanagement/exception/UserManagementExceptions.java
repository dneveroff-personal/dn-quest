package dn.quest.usermanagement.exception;

/**
 * Кастомные исключения для User Management Service
 */
public class UserManagementExceptions {

    /**
     * Исключение когда пользователь уже существует
     */
    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }

        public UserAlreadyExistsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение когда пользователь не найден
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }

        public UserNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение когда профиль пользователя не найден
     */
    public static class UserProfileNotFoundException extends RuntimeException {
        public UserProfileNotFoundException(String message) {
            super(message);
        }

        public UserProfileNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение когда настройки пользователя не найдены
     */
    public static class UserSettingsNotFoundException extends RuntimeException {
        public UserSettingsNotFoundException(String message) {
            super(message);
        }

        public UserSettingsNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение когда статистика пользователя не найдена
     */
    public static class UserStatisticsNotFoundException extends RuntimeException {
        public UserStatisticsNotFoundException(String message) {
            super(message);
        }

        public UserStatisticsNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение когда операция недопустима
     */
    public static class IllegalOperationException extends RuntimeException {
        public IllegalOperationException(String message) {
            super(message);
        }

        public IllegalOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}