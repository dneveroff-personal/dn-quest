package dn.quest.notification.service.impl;

import dn.quest.notification.service.RateLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация сервиса для ограничения частоты отправки уведомлений
 */
@Service
public class RateLimitingServiceImpl implements RateLimitingService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingServiceImpl.class);

    // Хранилище счетчиков для пользователей
    private final ConcurrentHashMap<String, UserCounter> userCounters = new ConcurrentHashMap<>();
    
    // Хранилище счетчиков для IP адресов
    private final ConcurrentHashMap<String, IpCounter> ipCounters = new ConcurrentHashMap<>();
    
    // Черный список пользователей
    private final ConcurrentHashMap<Long, BlacklistEntry> userBlacklist = new ConcurrentHashMap<>();
    
    // Блокированные IP адреса
    private final ConcurrentHashMap<String, BlacklistEntry> ipBlocks = new ConcurrentHashMap<>();
    
    // Глобальный счетчик
    private final AtomicLong globalCounter = new AtomicLong(0);
    private volatile LocalDateTime globalResetTime = LocalDateTime.now().plusHours(1);

    @Value("${app.notification.rate-limit.user-per-minute:10}")
    private int userNotificationsPerMinute;

    @Value("${app.notification.rate-limit.user-per-hour:100}")
    private int userNotificationsPerHour;

    @Value("${app.notification.rate-limit.user-per-day:1000}")
    private int userNotificationsPerDay;

    @Value("${app.notification.rate-limit.ip-per-minute:20}")
    private int ipRequestsPerMinute;

    @Value("${app.notification.rate-limit.ip-per-hour:500}")
    private int ipRequestsPerHour;

    @Value("${app.notification.rate-limit.global-per-minute:1000}")
    private int globalNotificationsPerMinute;

    @Value("${app.notification.rate-limit.global-per-hour:10000}")
    private int globalNotificationsPerHour;

    @Value("${app.notification.rate-limit.blacklist-duration-minutes:60}")
    private int blacklistDurationMinutes;

    @Override
    public boolean canSendNotification(Long userId, String notificationType) {
        // Проверяем черный список
        if (isUserBlacklisted(userId)) {
            logger.debug("User {} is blacklisted", userId);
            return false;
        }

        String key = userId.toString();
        UserCounter counter = userCounters.computeIfAbsent(key, k -> new UserCounter());
        
        // Очищаем старые счетчики
        counter.cleanup();
        
        // Проверяем лимиты
        if (counter.minuteCount >= userNotificationsPerMinute) {
            logger.debug("User {} exceeded minute limit: {}/{}", 
                        userId, counter.minuteCount, userNotificationsPerMinute);
            return false;
        }
        
        if (counter.hourCount >= userNotificationsPerHour) {
            logger.debug("User {} exceeded hour limit: {}/{}", 
                        userId, counter.hourCount, userNotificationsPerHour);
            return false;
        }
        
        if (counter.dayCount >= userNotificationsPerDay) {
            logger.debug("User {} exceeded day limit: {}/{}", 
                        userId, counter.dayCount, userNotificationsPerDay);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean canSendFromIp(String ipAddress, String endpoint) {
        // Проверяем блокировку IP
        BlacklistEntry block = ipBlocks.get(ipAddress);
        if (block != null && block.getExpiryTime().isAfter(LocalDateTime.now())) {
            logger.debug("IP {} is blocked: {}", ipAddress, block.getReason());
            return false;
        }

        IpCounter counter = ipCounters.computeIfAbsent(ipAddress, k -> new IpCounter());
        
        // Очищаем старые счетчики
        counter.cleanup();
        
        // Проверяем лимиты
        if (counter.minuteCount >= ipRequestsPerMinute) {
            logger.debug("IP {} exceeded minute limit: {}/{}", 
                        ipAddress, counter.minuteCount, ipRequestsPerMinute);
            return false;
        }
        
        if (counter.hourCount >= ipRequestsPerHour) {
            logger.debug("IP {} exceeded hour limit: {}/{}", 
                        ipAddress, counter.hourCount, ipRequestsPerHour);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean canSendGlobally() {
        // Проверяем и обновляем время сброса глобального счетчика
        if (LocalDateTime.now().isAfter(globalResetTime)) {
            globalCounter.set(0);
            globalResetTime = LocalDateTime.now().plusHours(1);
        }
        
        long currentCount = globalCounter.get();
        
        if (currentCount >= globalNotificationsPerHour) {
            logger.debug("Global limit exceeded: {}/{}", currentCount, globalNotificationsPerHour);
            return false;
        }
        
        return true;
    }

    @Override
    public void recordNotificationAttempt(Long userId, String notificationType) {
        String key = userId.toString();
        UserCounter counter = userCounters.get(key);
        if (counter != null) {
            counter.increment();
        }
        
        // Увеличиваем глобальный счетчик
        globalCounter.incrementAndGet();
    }

    @Override
    public void recordIpRequest(String ipAddress, String endpoint) {
        IpCounter counter = ipCounters.get(ipAddress);
        if (counter != null) {
            counter.increment();
        }
    }

    @Override
    public void recordGlobalNotification() {
        globalCounter.incrementAndGet();
    }

    @Override
    public UserRateLimitStatus getUserRateLimitStatus(Long userId) {
        String key = userId.toString();
        UserCounter counter = userCounters.get(key);
        if (counter == null) {
            counter = new UserCounter();
        }
        
        counter.cleanup();
        
        UserRateLimitStatus status = new UserRateLimitStatus();
        status.setUserId(userId);
        
        // Текущие счетчики
        status.setCurrentCounts(Map.of(
            "minute", counter.minuteCount,
            "hour", counter.hourCount,
            "day", counter.dayCount
        ));
        
        // Лимиты
        status.setLimits(Map.of(
            "minute", userNotificationsPerMinute,
            "hour", userNotificationsPerHour,
            "day", userNotificationsPerDay
        ));
        
        // Время сброса
        status.setResetTimes(Map.of(
            "minute", counter.minuteResetTime,
            "hour", counter.hourResetTime,
            "day", counter.dayResetTime
        ));
        
        // Черный список
        BlacklistEntry blacklist = userBlacklist.get(userId);
        if (blacklist != null && blacklist.getExpiryTime().isAfter(LocalDateTime.now())) {
            status.setBlacklisted(true);
            status.setBlacklistReason(blacklist.getReason());
            status.setBlacklistExpiry(blacklist.getExpiryTime());
        } else {
            status.setBlacklisted(false);
        }
        
        return status;
    }

    @Override
    public IpRateLimitStatus getIpRateLimitStatus(String ipAddress) {
        IpCounter counter = ipCounters.get(ipAddress);
        if (counter == null) {
            counter = new IpCounter();
        }
        
        counter.cleanup();
        
        IpRateLimitStatus status = new IpRateLimitStatus();
        status.setIpAddress(ipAddress);
        
        // Текущие счетчики
        status.setCurrentCounts(Map.of(
            "minute", counter.minuteCount,
            "hour", counter.hourCount
        ));
        
        // Лимиты
        status.setLimits(Map.of(
            "minute", ipRequestsPerMinute,
            "hour", ipRequestsPerHour
        ));
        
        // Время сброса
        status.setResetTimes(Map.of(
            "minute", counter.minuteResetTime,
            "hour", counter.hourResetTime
        ));
        
        // Блокировка
        BlacklistEntry block = ipBlocks.get(ipAddress);
        if (block != null && block.getExpiryTime().isAfter(LocalDateTime.now())) {
            status.setBlocked(true);
            status.setBlockReason(block.getReason());
            status.setBlockExpiry(block.getExpiryTime());
        } else {
            status.setBlocked(false);
        }
        
        return status;
    }

    @Override
    public GlobalRateLimitStatus getGlobalRateLimitStatus() {
        GlobalRateLimitStatus status = new GlobalRateLimitStatus();
        status.setCurrentCount(globalCounter.get());
        status.setLimit(globalNotificationsPerHour);
        status.setResetTime(globalResetTime);
        status.setThrottled(globalCounter.get() >= globalNotificationsPerHour);
        
        return status;
    }

    @Override
    public void resetUserCounters(Long userId) {
        userCounters.remove(userId.toString());
        logger.info("Reset counters for user: {}", userId);
    }

    @Override
    public void resetIpCounters(String ipAddress) {
        ipCounters.remove(ipAddress);
        logger.info("Reset counters for IP: {}", ipAddress);
    }

    @Override
    public boolean isUserBlacklisted(Long userId) {
        BlacklistEntry entry = userBlacklist.get(userId);
        return entry != null && entry.getExpiryTime().isAfter(LocalDateTime.now());
    }

    @Override
    public void blacklistUser(Long userId, String reason, long durationMinutes) {
        BlacklistEntry entry = new BlacklistEntry(reason, LocalDateTime.now().plusMinutes(durationMinutes));
        userBlacklist.put(userId, entry);
        logger.warn("User {} blacklisted for {} minutes. Reason: {}", userId, durationMinutes, reason);
    }

    @Override
    public void removeFromBlacklist(Long userId) {
        userBlacklist.remove(userId);
        logger.info("User {} removed from blacklist", userId);
    }

    // Внутренние классы для хранения счетчиков

    private static class UserCounter {
        private int minuteCount = 0;
        private int hourCount = 0;
        private int dayCount = 0;
        private LocalDateTime minuteResetTime = LocalDateTime.now().plusMinutes(1);
        private LocalDateTime hourResetTime = LocalDateTime.now().plusHours(1);
        private LocalDateTime dayResetTime = LocalDateTime.now().plusDays(1);

        public void increment() {
            cleanup();
            minuteCount++;
            hourCount++;
            dayCount++;
        }

        public void cleanup() {
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isAfter(minuteResetTime)) {
                minuteCount = 0;
                minuteResetTime = now.plusMinutes(1);
            }
            
            if (now.isAfter(hourResetTime)) {
                hourCount = 0;
                hourResetTime = now.plusHours(1);
            }
            
            if (now.isAfter(dayResetTime)) {
                dayCount = 0;
                dayResetTime = now.plusDays(1);
            }
        }
    }

    private static class IpCounter {
        private int minuteCount = 0;
        private int hourCount = 0;
        private LocalDateTime minuteResetTime = LocalDateTime.now().plusMinutes(1);
        private LocalDateTime hourResetTime = LocalDateTime.now().plusHours(1);

        public void increment() {
            cleanup();
            minuteCount++;
            hourCount++;
        }

        public void cleanup() {
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isAfter(minuteResetTime)) {
                minuteCount = 0;
                minuteResetTime = now.plusMinutes(1);
            }
            
            if (now.isAfter(hourResetTime)) {
                hourCount = 0;
                hourResetTime = now.plusHours(1);
            }
        }
    }

    private static class BlacklistEntry {
        private final String reason;
        private final LocalDateTime expiryTime;

        public BlacklistEntry(String reason, LocalDateTime expiryTime) {
            this.reason = reason;
            this.expiryTime = expiryTime;
        }

        public String getReason() { return reason; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
    }
}