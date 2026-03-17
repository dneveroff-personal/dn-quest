package dn.quest.shared.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Утилитарный класс для работы с датами и временем
 */
public final class DateTimeUtils {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Moscow");
    
    private DateTimeUtils() {
        // Утилитарный класс
    }
    
    /**
     * Форматирует LocalDateTime в строку по умолчанию
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : null;
    }
    
    /**
     * Форматирует LocalDateTime в строку ISO
     */
    public static String formatIso(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_FORMATTER) : null;
    }
    
    /**
     * Парсит строку в LocalDateTime
     */
    public static LocalDateTime parse(String dateTimeString) {
        return dateTimeString != null ? LocalDateTime.parse(dateTimeString, DEFAULT_FORMATTER) : null;
    }
    
    /**
     * Парсит строку в LocalDateTime из ISO формата
     */
    public static LocalDateTime parseIso(String dateTimeString) {
        return dateTimeString != null ? LocalDateTime.parse(dateTimeString, ISO_FORMATTER) : null;
    }
    
    /**
     * Возвращает текущее время в UTC
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
    
    /**
     * Возвращает текущее время в часовом поясе по умолчанию
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }
    
    /**
     * Конвертирует LocalDateTime в UTC
     */
    public static LocalDateTime toUtc(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(DEFAULT_ZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }
    
    /**
     * Конвертирует LocalDateTime из UTC в часовой пояс по умолчанию
     */
    public static LocalDateTime fromUtc(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(DEFAULT_ZONE).toLocalDateTime() : null;
    }
    
    /**
     * Вычисляет разницу в секундах между двумя датами
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }
    
    /**
     * Вычисляет разницу в минутах между двумя датами
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }
    
    /**
     * Вычисляет разницу в часах между двумя датами
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }
    
    /**
     * Вычисляет разницу в днях между двумя датами
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * Добавляет указанное количество секунд к дате
     */
    public static LocalDateTime addSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime != null ? dateTime.plusSeconds(seconds) : null;
    }
    
    /**
     * Добавляет указанное количество минут к дате
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }
    
    /**
     * Добавляет указанное количество часов к дате
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }
    
    /**
     * Добавляет указанное количество дней к дате
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.plusDays(days) : null;
    }
    
    /**
     * Проверяет, находится ли дата в будущем
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(now());
    }
    
    /**
     * Проверяет, находится ли дата в прошлом
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(now());
    }
    
    /**
     * Проверяет, находится ли дата в указанном диапазоне
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return dateTime != null && start != null && end != null &&
               !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
    
    /**
     * Возвращает начало дня для указанной даты
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atStartOfDay() : null;
    }
    
    /**
     * Возвращает конец дня для указанной даты
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atTime(23, 59, 59) : null;
    }
}