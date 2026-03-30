package dn.quest.shared.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилитарный класс для маппинга между DTO и сущностями
 */
@Component
public final class DtoMapper {
    
    private DtoMapper() {
        // Утилитарный класс
    }
    
    /**
     * Безопасное копирование поля
     */
    public static <T> T safeCopy(T source) {
        return source;
    }
    
    /**
     * Безопасное копирование строки
     */
    public static String safeCopyString(String source) {
        return source;
    }
    
    /**
     * Безопасное копирование даты
     */
    public static LocalDateTime safeCopyDateTime(LocalDateTime source) {
        return source;
    }
    
    /**
     * Конвертирует коллекцию в список
     */
    public static <T> List<T> toList(Collection<T> collection) {
        return collection != null ? 
            collection.stream().collect(Collectors.toList()) : 
            List.of();
    }
    
    /**
     * Конвертирует коллекцию в set
     */
    public static <T> Set<T> toSet(Collection<T> collection) {
        return collection != null ? 
            collection.stream().collect(Collectors.toSet()) : 
            Set.of();
    }
    
    /**
     * Применяет функцию маппинга к каждому элементу коллекции
     */
    public static <S, T> List<T> mapList(Collection<S> source, java.util.function.Function<S, T> mapper) {
        return source != null ? 
            source.stream()
                .map(mapper)
                .collect(Collectors.toList()) : 
            List.of();
    }
    
    /**
     * Применяет функцию маппинга к каждому элементу коллекции и возвращает set
     */
    public static <S, T> Set<T> mapSet(Collection<S> source, java.util.function.Function<S, T> mapper) {
        return source != null ? 
            source.stream()
                .map(mapper)
                .collect(Collectors.toSet()) : 
            Set.of();
    }
    
    /**
     * Безопасное получение значения из enum
     */
    public static <T extends Enum<T>> T safeGetEnum(Class<T> enumClass, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Безопасное получение значения из enum с значением по умолчанию
     */
    public static <T extends Enum<T>> T safeGetEnum(Class<T> enumClass, String value, T defaultValue) {
        T result = safeGetEnum(enumClass, value);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Конвертирует enum в строку
     */
    public static String enumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : null;
    }
    
    /**
     * Конвертирует enum в displayName
     */
    public static String enumToDisplayName(Enum<?> enumValue) {
        if (enumValue == null) {
            return null;
        }
        
        try {
            // Пытаемся вызвать метод getDisplayName()
            java.lang.reflect.Method method = enumValue.getClass().getMethod("getDisplayName");
            return (String) method.invoke(enumValue);
        } catch (Exception e) {
            // Если метод не найден, возвращаем name()
            return enumValue.name();
        }
    }
    
    /**
     * Безопасное парсинг строки в Long
     */
    public static Long safeParseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Безопасное парсинг строки в Integer
     */
    public static Integer safeParseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Безопасное парсинг строки в Boolean
     */
    public static Boolean safeParseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String trimmedValue = value.trim().toLowerCase();
        return "true".equals(trimmedValue) || "yes".equals(trimmedValue) || "1".equals(trimmedValue);
    }
    
    /**
     * Обрезает строку до указанной длины
     */
    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
    
    /**
     * Проверяет, что строка не null и не пустая
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Проверяет, что строка null или пустая
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * Возвращает строку или значение по умолчанию
     */
    public static String defaultIfEmpty(String value, String defaultValue) {
        return isNotEmpty(value) ? value : defaultValue;
    }
    
    /**
     * Безопасное сравнение двух объектов
     */
    public static boolean safeEquals(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }
    
    /**
     * Вычисляет хэш-код объекта с защитой от null
     */
    public static int safeHashCode(Object obj) {
        return obj != null ? obj.hashCode() : 0;
    }
}