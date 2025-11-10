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

    /**
     * Исключение когда пользователь уже заблокирован
     */
    public static class UserAlreadyBlockedException extends IllegalOperationException {
        public UserAlreadyBlockedException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда пользователь не заблокирован
     */
    public static class UserNotBlockedException extends IllegalOperationException {
        public UserNotBlockedException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда пользователь уже активен
     */
    public static class UserAlreadyActiveException extends IllegalOperationException {
        public UserAlreadyActiveException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда пользователь неактивен
     */
    public static class UserNotActiveException extends IllegalOperationException {
        public UserNotActiveException(String message) {
            super(message);
        }
    }

    /**
     * Исключение ошибки внешнего сервиса
     */
    public static class ExternalServiceException extends RuntimeException {
        public ExternalServiceException(String message) {
            super(message);
        }

        public ExternalServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение ошибки бизнес-логики
     */
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }

        public BusinessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение когда аватар недействителен
     */
    public static class InvalidAvatarException extends BusinessException {
        public InvalidAvatarException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда настройки недействительны
     */
    public static class InvalidSettingsException extends BusinessException {
        public InvalidSettingsException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда статистика недействительна
     */
    public static class InvalidStatisticsException extends BusinessException {
        public InvalidStatisticsException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда данные профиля недействительны
     */
    public static class InvalidProfileDataException extends BusinessException {
        public InvalidProfileDataException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда роль пользователя недействительна
     */
    public static class InvalidRoleException extends BusinessException {
        public InvalidRoleException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда уровень пользователя недействителен
     */
    public static class InvalidLevelException extends BusinessException {
        public InvalidLevelException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда опыт пользователя недействителен
     */
    public static class InvalidExperienceException extends BusinessException {
        public InvalidExperienceException(String message) {
            super(message);
        }
    }

    /**
     * Исключение когда очки пользователя недействительны
     */
    public static class InvalidScoreException extends BusinessException {
        public InvalidScoreException(String message) {
            super(message);
        }
    }
}