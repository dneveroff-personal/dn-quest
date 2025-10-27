package dn.quest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Утилитарный класс для работы с датами и временем
 */
@Slf4j
@Component
public class DateTimeUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Конвертирует Instant в строку формата dd.MM.yyyy HH:mm
     */
    public String formatDateTime(Instant instant) {
        if (instant == null) {
            return "";
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
    }

    /**
     * Конвертирует Instant в строку формата dd.MM.yyyy
     */
    public String formatDate(Instant instant) {
        if (instant == null) {
            return "";
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DATE_FORMATTER);
    }

    /**
     * Конвертирует Instant в строку формата HH:mm
     */
    public String formatTime(Instant instant) {
        if (instant == null) {
            return "";
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(TIME_FORMATTER);
    }

    /**
     * Вычисляет разницу между двумя моментами времени в человекочитаемом формате
     */
    public String getTimeUntil(Instant target) {
        if (target == null) {
            return "";
        }
        
        Instant now = Instant.now();
        if (target.isBefore(now)) {
            return "уже началось";
        }
        
        long days = ChronoUnit.DAYS.between(now, target);
        long hours = ChronoUnit.HOURS.between(now, target) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, target) % 60;
        
        if (days > 0) {
            return String.format("%dд %dч %dм", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dч %dм", hours, minutes);
        } else {
            return String.format("%dм", minutes);
        }
    }

    /**
     * Вычисляет разницу между двумя моментами времени в секундах
     */
    public long getSecondsBetween(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * Проверяет, истекло ли указанное количество секунд с указанного момента
     */
    public boolean hasElapsed(Instant start, long seconds) {
        if (start == null) {
            return false;
        }
        return Instant.now().isAfter(start.plusSeconds(seconds));
    }

    /**
     * Возвращает текущий момент времени
     */
    public Instant now() {
        return Instant.now();
    }

    /**
     * Добавляет указанное количество секунд к моменту времени
     */
    public Instant addSeconds(Instant instant, long seconds) {
        if (instant == null) {
            return null;
        }
        return instant.plusSeconds(seconds);
    }

    /**
     * Вычитает указанное количество секунд из момента времени
     */
    public Instant subtractSeconds(Instant instant, long seconds) {
        if (instant == null) {
            return null;
        }
        return instant.minusSeconds(seconds);
    }

    /**
     * Форматирует длительность в секундах в строку HH:MM:SS
     */
    public String formatDuration(long totalSeconds) {
        if (totalSeconds < 0) {
            return "00:00:00";
        }
        
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Форматирует длительность в секундах в строку HH:MM:SS (перегруженный метод для int)
     */
    public String formatDuration(int totalSeconds) {
        return formatDuration((long) totalSeconds);
    }

    /**
     * Форматирует длительность в секундах в строку Xд Yч Zм
     */
    public String formatDurationHuman(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0м";
        }
        
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append("д ");
        }
        if (hours > 0) {
            result.append(hours).append("ч ");
        }
        if (minutes > 0 || result.length() == 0) {
            result.append(minutes).append("м");
        }
        
        return result.toString().trim();
    }
}