package dn.quest.notification.service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Сервис для ограничения частоты отправки уведомлений (Rate Limiting)
 */
public interface RateLimitingService {

    /**
     * Проверить, может ли пользователь отправить уведомление
     */
    boolean canSendNotification(Long userId, String notificationType);

    /**
     * Проверить, может ли IP адрес отправить запрос
     */
    boolean canSendFromIp(String ipAddress, String endpoint);

    /**
     * Проверить глобальные лимиты
     */
    boolean canSendGlobally();

    /**
     * Зарегистрировать попытку отправки уведомления
     */
    void recordNotificationAttempt(Long userId, String notificationType);

    /**
     * Зарегистрировать запрос от IP
     */
    void recordIpRequest(String ipAddress, String endpoint);

    /**
     * Зарегистрировать глобальную отправку
     */
    void recordGlobalNotification();

    /**
     * Получить статистику по лимитам для пользователя
     */
    UserRateLimitStatus getUserRateLimitStatus(Long userId);

    /**
     * Получить статистику по лимитам для IP
     */
    IpRateLimitStatus getIpRateLimitStatus(String ipAddress);

    /**
     * Получить глобальную статистику по лимитам
     */
    GlobalRateLimitStatus getGlobalRateLimitStatus();

    /**
     * Сбросить счетчики для пользователя
     */
    void resetUserCounters(Long userId);

    /**
     * Сбросить счетчики для IP
     */
    void resetIpCounters(String ipAddress);

    /**
     * Проверить, находится ли пользователь в черном списке
     */
    boolean isUserBlacklisted(Long userId);

    /**
     * Добавить пользователя в черный список
     */
    void blacklistUser(Long userId, String reason, long durationMinutes);

    /**
     * Удалить пользователя из черного списка
     */
    void removeFromBlacklist(Long userId);

    // DTO классы

    class UserRateLimitStatus {
        private Long userId;
        private Map<String, Integer> currentCounts;
        private Map<String, Integer> limits;
        private Map<String, LocalDateTime> resetTimes;
        private boolean blacklisted;
        private String blacklistReason;
        private LocalDateTime blacklistExpiry;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Map<String, Integer> getCurrentCounts() { return currentCounts; }
        public void setCurrentCounts(Map<String, Integer> currentCounts) { this.currentCounts = currentCounts; }

        public Map<String, Integer> getLimits() { return limits; }
        public void setLimits(Map<String, Integer> limits) { this.limits = limits; }

        public Map<String, LocalDateTime> getResetTimes() { return resetTimes; }
        public void setResetTimes(Map<String, LocalDateTime> resetTimes) { this.resetTimes = resetTimes; }

        public boolean isBlacklisted() { return blacklisted; }
        public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }

        public String getBlacklistReason() { return blacklistReason; }
        public void setBlacklistReason(String blacklistReason) { this.blacklistReason = blacklistReason; }

        public LocalDateTime getBlacklistExpiry() { return blacklistExpiry; }
        public void setBlacklistExpiry(LocalDateTime blacklistExpiry) { this.blacklistExpiry = blacklistExpiry; }
    }

    class IpRateLimitStatus {
        private String ipAddress;
        private Map<String, Integer> currentCounts;
        private Map<String, Integer> limits;
        private Map<String, LocalDateTime> resetTimes;
        private boolean blocked;
        private String blockReason;
        private LocalDateTime blockExpiry;

        // Getters and setters
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public Map<String, Integer> getCurrentCounts() { return currentCounts; }
        public void setCurrentCounts(Map<String, Integer> currentCounts) { this.currentCounts = currentCounts; }

        public Map<String, Integer> getLimits() { return limits; }
        public void setLimits(Map<String, Integer> limits) { this.limits = limits; }

        public Map<String, LocalDateTime> getResetTimes() { return resetTimes; }
        public void setResetTimes(Map<String, LocalDateTime> resetTimes) { this.resetTimes = resetTimes; }

        public boolean isBlocked() { return blocked; }
        public void setBlocked(boolean blocked) { this.blocked = blocked; }

        public String getBlockReason() { return blockReason; }
        public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

        public LocalDateTime getBlockExpiry() { return blockExpiry; }
        public void setBlockExpiry(LocalDateTime blockExpiry) { this.blockExpiry = blockExpiry; }
    }

    class GlobalRateLimitStatus {
        private long currentCount;
        private long limit;
        private LocalDateTime resetTime;
        private boolean throttled;

        // Getters and setters
        public long getCurrentCount() { return currentCount; }
        public void setCurrentCount(long currentCount) { this.currentCount = currentCount; }

        public long getLimit() { return limit; }
        public void setLimit(long limit) { this.limit = limit; }

        public LocalDateTime getResetTime() { return resetTime; }
        public void setResetTime(LocalDateTime resetTime) { this.resetTime = resetTime; }

        public boolean isThrottled() { return throttled; }
        public void setThrottled(boolean throttled) { this.throttled = throttled; }
    }
}