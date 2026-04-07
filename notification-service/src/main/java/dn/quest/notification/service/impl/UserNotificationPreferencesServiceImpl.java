package dn.quest.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.notification.entity.UserNotificationPreferences;
import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.repository.NotificationRepository;
import dn.quest.notification.repository.UserNotificationPreferencesRepository;
import dn.quest.notification.service.UserNotificationPreferencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления пользовательскими предпочтениями уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserNotificationPreferencesServiceImpl implements UserNotificationPreferencesService {

    private final UserNotificationPreferencesRepository preferencesRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.notification.defaults.email-enabled:true}")
    private boolean defaultEmailEnabled;

    @Value("${app.notification.defaults.push-enabled:true}")
    private boolean defaultPushEnabled;

    @Value("${app.notification.defaults.in-app-enabled:true}")
    private boolean defaultInAppEnabled;

    @Value("${app.notification.defaults.telegram-enabled:false}")
    private boolean defaultTelegramEnabled;

    @Value("${app.notification.defaults.sms-enabled:false}")
    private boolean defaultSmsEnabled;

    @Value("${app.notification.defaults.language:ru}")
    private String defaultLanguage;

    @Value("${app.notification.defaults.time-zone:UTC}")
    private String defaultTimeZone;

    @Value("${app.notification.defaults.max-per-hour:20}")
    private int defaultMaxPerHour;

    @Value("${app.notification.defaults.max-per-day:200}")
    private int defaultMaxPerDay;

    @Override
    public UserNotificationPreferences createPreferences(UUID userId) {
        log.info("Creating notification preferences for user: {}", userId);

        if (preferencesRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Preferences already exist for user: " + userId);
        }

        UserNotificationPreferences preferences = UserNotificationPreferences.builder()
                .userId(userId)
                .emailEnabled(defaultEmailEnabled)
                .pushEnabled(defaultPushEnabled)
                .inAppEnabled(defaultInAppEnabled)
                .telegramEnabled(defaultTelegramEnabled)
                .smsEnabled(defaultSmsEnabled)
                .welcomeEnabled(true)
                .questEnabled(true)
                .gameEnabled(true)
                .teamEnabled(true)
                .systemEnabled(true)
                .securityEnabled(true)
                .marketingEnabled(false)
                .reminderEnabled(true)
                .doNotDisturbEnabled(false)
                .maxNotificationsPerHour(defaultMaxPerHour)
                .maxNotificationsPerDay(defaultMaxPerDay)
                .preferredLanguage(defaultLanguage)
                .timeZone(defaultTimeZone)
                .build();

        UserNotificationPreferences saved = preferencesRepository.save(preferences);
        log.info("Preferences created successfully for user: {}", userId);
        
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserNotificationPreferences> getPreferences(UUID userId) {
        return preferencesRepository.findByUserId(userId);
    }

    @Override
    public UserNotificationPreferences updatePreferences(UUID userId, UserNotificationPreferences preferences) {
        log.info("Updating notification preferences for user: {}", userId);

        UserNotificationPreferences existing = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user: " + userId));

        // Обновление полей
        existing.setEmailEnabled(preferences.getEmailEnabled());
        existing.setPushEnabled(preferences.getPushEnabled());
        existing.setInAppEnabled(preferences.getInAppEnabled());
        existing.setTelegramEnabled(preferences.getTelegramEnabled());
        existing.setSmsEnabled(preferences.getSmsEnabled());
        existing.setWelcomeEnabled(preferences.getWelcomeEnabled());
        existing.setQuestEnabled(preferences.getQuestEnabled());
        existing.setGameEnabled(preferences.getGameEnabled());
        existing.setTeamEnabled(preferences.getTeamEnabled());
        existing.setSystemEnabled(preferences.getSystemEnabled());
        existing.setSecurityEnabled(preferences.getSecurityEnabled());
        existing.setMarketingEnabled(preferences.getMarketingEnabled());
        existing.setReminderEnabled(preferences.getReminderEnabled());
        existing.setDoNotDisturbEnabled(preferences.getDoNotDisturbEnabled());
        existing.setDoNotDisturbStartHour(preferences.getDoNotDisturbStartHour());
        existing.setDoNotDisturbEndHour(preferences.getDoNotDisturbEndHour());
        existing.setMaxNotificationsPerHour(preferences.getMaxNotificationsPerHour());
        existing.setMaxNotificationsPerDay(preferences.getMaxNotificationsPerDay());
        existing.setPreferredLanguage(preferences.getPreferredLanguage());
        existing.setTimeZone(preferences.getTimeZone());
        existing.setEmail(preferences.getEmail());
        existing.setPhone(preferences.getPhone());
        existing.setTelegramChatId(preferences.getTelegramChatId());
        existing.setFcmToken(preferences.getFcmToken());
        existing.setUpdatedBy(preferences.getUpdatedBy());

        UserNotificationPreferences saved = preferencesRepository.save(existing);
        log.info("Preferences updated successfully for user: {}", userId);
        
        return saved;
    }

    @Override
    public UserNotificationPreferences updatePreferences(UUID userId, Map<String, Object> updates) {
        log.info("Updating specific preferences for user: {}", userId);

        UserNotificationPreferences existing = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user: " + userId));

        // Применение обновлений
        updates.forEach((key, value) -> {
            switch (key) {
                case "emailEnabled":
                    existing.setEmailEnabled((Boolean) value);
                    break;
                case "pushEnabled":
                    existing.setPushEnabled((Boolean) value);
                    break;
                case "inAppEnabled":
                    existing.setInAppEnabled((Boolean) value);
                    break;
                case "telegramEnabled":
                    existing.setTelegramEnabled((Boolean) value);
                    break;
                case "smsEnabled":
                    existing.setSmsEnabled((Boolean) value);
                    break;
                case "welcomeEnabled":
                    existing.setWelcomeEnabled((Boolean) value);
                    break;
                case "questEnabled":
                    existing.setQuestEnabled((Boolean) value);
                    break;
                case "gameEnabled":
                    existing.setGameEnabled((Boolean) value);
                    break;
                case "teamEnabled":
                    existing.setTeamEnabled((Boolean) value);
                    break;
                case "systemEnabled":
                    existing.setSystemEnabled((Boolean) value);
                    break;
                case "securityEnabled":
                    existing.setSecurityEnabled((Boolean) value);
                    break;
                case "marketingEnabled":
                    existing.setMarketingEnabled((Boolean) value);
                    break;
                case "reminderEnabled":
                    existing.setReminderEnabled((Boolean) value);
                    break;
                case "doNotDisturbEnabled":
                    existing.setDoNotDisturbEnabled((Boolean) value);
                    break;
                case "doNotDisturbStartHour":
                    existing.setDoNotDisturbStartHour((Integer) value);
                    break;
                case "doNotDisturbEndHour":
                    existing.setDoNotDisturbEndHour((Integer) value);
                    break;
                case "maxNotificationsPerHour":
                    existing.setMaxNotificationsPerHour((Integer) value);
                    break;
                case "maxNotificationsPerDay":
                    existing.setMaxNotificationsPerDay((Integer) value);
                    break;
                case "preferredLanguage":
                    existing.setPreferredLanguage((String) value);
                    break;
                case "timeZone":
                    existing.setTimeZone((String) value);
                    break;
                case "email":
                    existing.setEmail((String) value);
                    break;
                case "phone":
                    existing.setPhone((String) value);
                    break;
                case "telegramChatId":
                    existing.setTelegramChatId((String) value);
                    break;
                case "fcmToken":
                    existing.setFcmToken((String) value);
                    break;
                default:
                    log.warn("Unknown preference key: {}", key);
            }
        });

        return preferencesRepository.save(existing);
    }

    @Override
    public void deletePreferences(UUID userId) {
        log.info("Deleting notification preferences for user: {}", userId);
        
        UserNotificationPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user: " + userId));

        preferencesRepository.delete(preferences);
        log.info("Preferences deleted successfully for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNotificationTypeEnabled(UUID userId, NotificationType type) {
        Optional<UserNotificationPreferences> preferences = getPreferences(userId);
        if (preferences.isEmpty()) {
            // Если предпочтений нет, используем значения по умолчанию
            return getDefaultTypeEnabled(type);
        }

        UserNotificationPreferences prefs = preferences.get();
        return switch (type) {
            case EMAIL -> prefs.getEmailEnabled();
            case PUSH -> prefs.getPushEnabled();
            case IN_APP -> prefs.getInAppEnabled();
            case TELEGRAM -> prefs.getTelegramEnabled();
            case SMS -> prefs.getSmsEnabled();
        };
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNotificationCategoryEnabled(UUID userId, String category) {
        Optional<UserNotificationPreferences> preferences = getPreferences(userId);
        if (preferences.isEmpty()) {
            return getDefaultCategoryEnabled(category);
        }

        UserNotificationPreferences prefs = preferences.get();
        return switch (category.toLowerCase()) {
            case "welcome" -> prefs.getWelcomeEnabled();
            case "quest" -> prefs.getQuestEnabled();
            case "game" -> prefs.getGameEnabled();
            case "team" -> prefs.getTeamEnabled();
            case "system" -> prefs.getSystemEnabled();
            case "security" -> prefs.getSecurityEnabled();
            case "marketing" -> prefs.getMarketingEnabled();
            case "reminder" -> prefs.getReminderEnabled();
            default -> true;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDoNotDisturbActive(UUID userId) {
        Optional<UserNotificationPreferences> preferences = getPreferences(userId);
        if (preferences.isEmpty()) {
            return false;
        }

        UserNotificationPreferences prefs = preferences.get();
        if (!prefs.getDoNotDisturbEnabled()) {
            return false;
        }

        Integer startHour = prefs.getDoNotDisturbStartHour();
        Integer endHour = prefs.getDoNotDisturbEndHour();
        
        if (startHour == null || endHour == null) {
            return false;
        }

        // Получение текущего времени в часовом поясе пользователя
        ZoneId userZone = ZoneId.of(prefs.getTimeZone() != null ? prefs.getTimeZone() : "UTC");
        ZonedDateTime userTime = ZonedDateTime.now(userZone);
        int currentHour = userTime.getHour();

        if (startHour <= endHour) {
            // Пример: 22:00 - 07:00
            return currentHour >= startHour && currentHour < endHour;
        } else {
            // Пример: 22:00 - 07:00 (через полночь)
            return currentHour >= startHour || currentHour < endHour;
        }
    }

    @Override
    public void updateFcmToken(UUID userId, String fcmToken) {
        preferencesRepository.updateFcmToken(userId, fcmToken);
        log.debug("Updated FCM token for user: {}", userId);
    }

    @Override
    public void updateTelegramChatId(UUID userId, String telegramChatId) {
        preferencesRepository.updateTelegramChatId(userId, telegramChatId);
        log.debug("Updated Telegram chat ID for user: {}", userId);
    }

    @Override
    public void updateEmail(UUID userId, String email) {
        preferencesRepository.updateEmail(userId, email);
        log.debug("Updated email for user: {}", userId);
    }

    @Override
    public void updatePhone(UUID userId, String phone) {
        preferencesRepository.updatePhone(userId, phone);
        log.debug("Updated phone for user: {}", userId);
    }

    @Override
    public UserNotificationPreferences toggleNotificationType(UUID userId, NotificationType type, boolean enabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(type.getValue() + "Enabled", enabled);
        return updatePreferences(userId, updates);
    }

    @Override
    public UserNotificationPreferences toggleNotificationCategory(UUID userId, String category, boolean enabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(category + "Enabled", enabled);
        return updatePreferences(userId, updates);
    }

    @Override
    public UserNotificationPreferences setDoNotDisturb(UUID userId, boolean enabled, Integer startHour, Integer endHour) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("doNotDisturbEnabled", enabled);
        if (enabled) {
            updates.put("doNotDisturbStartHour", startHour != null ? startHour : 22);
            updates.put("doNotDisturbEndHour", endHour != null ? endHour : 7);
        }
        return updatePreferences(userId, updates);
    }

    @Override
    public UserNotificationPreferences setNotificationLimits(UUID userId, Integer perHour, Integer perDay) {
        Map<String, Object> updates = new HashMap<>();
        if (perHour != null) {
            updates.put("maxNotificationsPerHour", perHour);
        }
        if (perDay != null) {
            updates.put("maxNotificationsPerDay", perDay);
        }
        return updatePreferences(userId, updates);
    }

    @Override
    public UserNotificationPreferences setPreferredLanguage(UUID userId, String language) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("preferredLanguage", language);
        return updatePreferences(userId, updates);
    }

    @Override
    public UserNotificationPreferences setTimeZone(UUID userId, String timeZone) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("timeZone", timeZone);
        return updatePreferences(userId, updates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserNotificationPreferences> getUsersForBulkNotification(Boolean emailEnabled, 
                                                                        Boolean pushEnabled, 
                                                                        Boolean telegramEnabled, 
                                                                        Boolean smsEnabled) {
        return preferencesRepository.findUsersForBulkNotification(
                emailEnabled, pushEnabled, telegramEnabled, smsEnabled);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPreferencesStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", preferencesRepository.count());
        stats.put("usersWithEmailEnabled", preferencesRepository.countUsersWithEmailEnabled());
        stats.put("usersWithPushEnabled", preferencesRepository.countUsersWithPushEnabled());
        stats.put("usersWithTelegramEnabled", preferencesRepository.countUsersWithTelegramEnabled());
        stats.put("usersWithSmsEnabled", preferencesRepository.countUsersWithSmsEnabled());

        return stats;
    }

    @Override
    public UserNotificationPreferences resetToDefaults(UUID userId) {
        log.info("Resetting notification preferences to defaults for user: {}", userId);

        UserNotificationPreferences existing = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user: " + userId));

        existing.setEmailEnabled(defaultEmailEnabled);
        existing.setPushEnabled(defaultPushEnabled);
        existing.setInAppEnabled(defaultInAppEnabled);
        existing.setTelegramEnabled(defaultTelegramEnabled);
        existing.setSmsEnabled(defaultSmsEnabled);
        existing.setWelcomeEnabled(true);
        existing.setQuestEnabled(true);
        existing.setGameEnabled(true);
        existing.setTeamEnabled(true);
        existing.setSystemEnabled(true);
        existing.setSecurityEnabled(true);
        existing.setMarketingEnabled(false);
        existing.setReminderEnabled(true);
        existing.setDoNotDisturbEnabled(false);
        existing.setDoNotDisturbStartHour(null);
        existing.setDoNotDisturbEndHour(null);
        existing.setMaxNotificationsPerHour(defaultMaxPerHour);
        existing.setMaxNotificationsPerDay(defaultMaxPerDay);
        existing.setPreferredLanguage(defaultLanguage);
        existing.setTimeZone(defaultTimeZone);

        return preferencesRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> exportPreferences(UUID userId) {
        UserNotificationPreferences preferences = getPreferences(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user: " + userId));

        Map<String, Object> export = new HashMap<>();
        export.put("userId", preferences.getUserId());
        export.put("emailEnabled", preferences.getEmailEnabled());
        export.put("pushEnabled", preferences.getPushEnabled());
        export.put("inAppEnabled", preferences.getInAppEnabled());
        export.put("telegramEnabled", preferences.getTelegramEnabled());
        export.put("smsEnabled", preferences.getSmsEnabled());
        export.put("welcomeEnabled", preferences.getWelcomeEnabled());
        export.put("questEnabled", preferences.getQuestEnabled());
        export.put("gameEnabled", preferences.getGameEnabled());
        export.put("teamEnabled", preferences.getTeamEnabled());
        export.put("systemEnabled", preferences.getSystemEnabled());
        export.put("securityEnabled", preferences.getSecurityEnabled());
        export.put("marketingEnabled", preferences.getMarketingEnabled());
        export.put("reminderEnabled", preferences.getReminderEnabled());
        export.put("doNotDisturbEnabled", preferences.getDoNotDisturbEnabled());
        export.put("doNotDisturbStartHour", preferences.getDoNotDisturbStartHour());
        export.put("doNotDisturbEndHour", preferences.getDoNotDisturbEndHour());
        export.put("maxNotificationsPerHour", preferences.getMaxNotificationsPerHour());
        export.put("maxNotificationsPerDay", preferences.getMaxNotificationsPerDay());
        export.put("preferredLanguage", preferences.getPreferredLanguage());
        export.put("timeZone", preferences.getTimeZone());

        return export;
    }

    @Override
    public UserNotificationPreferences importPreferences(UUID userId, Map<String, Object> data) {
        log.info("Importing notification preferences for user: {}", userId);

        UserNotificationPreferences preferences = getPreferences(userId)
                .orElse(createPreferences(userId));

        return updatePreferences(userId, data);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkNotificationLimits(UUID userId) {
        Optional<UserNotificationPreferences> preferences = getPreferences(userId);
        if (preferences.isEmpty()) {
            return true; // Если предпочтений нет, лимиты не применяются
        }

        UserNotificationPreferences prefs = preferences.get();
        
        // Проверка лимита в час
        if (prefs.getMaxNotificationsPerHour() != null) {
            Instant oneHourAgo = Instant.now().minusSeconds(3600);
            long countLastHour = notificationRepository.countByUserIdAndCreatedAtBetween(userId, oneHourAgo, Instant.now());
            if (countLastHour >= prefs.getMaxNotificationsPerHour()) {
                return false;
            }
        }

        // Проверка лимита в день
        if (prefs.getMaxNotificationsPerDay() != null) {
            Instant oneDayAgo = Instant.now().minusSeconds(86400);
            long countLastDay = notificationRepository.countByUserIdAndCreatedAtBetween(userId, oneDayAgo, Instant.now());
            return countLastDay < prefs.getMaxNotificationsPerDay();
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationType> getAvailableChannels(UUID userId) {
        Optional<UserNotificationPreferences> preferences = getPreferences(userId);
        if (preferences.isEmpty()) {
            return Arrays.asList(NotificationType.EMAIL, NotificationType.PUSH, NotificationType.IN_APP);
        }

        UserNotificationPreferences prefs = preferences.get();
        List<NotificationType> channels = new ArrayList<>();

        if (prefs.getEmailEnabled() && prefs.getEmail() != null) {
            channels.add(NotificationType.EMAIL);
        }
        if (prefs.getPushEnabled() && prefs.getFcmToken() != null) {
            channels.add(NotificationType.PUSH);
        }
        if (prefs.getInAppEnabled()) {
            channels.add(NotificationType.IN_APP);
        }
        if (prefs.getTelegramEnabled() && prefs.getTelegramChatId() != null) {
            channels.add(NotificationType.TELEGRAM);
        }
        if (prefs.getSmsEnabled() && prefs.getPhone() != null) {
            channels.add(NotificationType.SMS);
        }

        return channels;
    }

    /**
     * Получить значение по умолчанию для типа уведомлений
     */
    private boolean getDefaultTypeEnabled(NotificationType type) {
        return switch (type) {
            case EMAIL -> defaultEmailEnabled;
            case PUSH -> defaultPushEnabled;
            case IN_APP -> defaultInAppEnabled;
            case TELEGRAM -> defaultTelegramEnabled;
            case SMS -> defaultSmsEnabled;
        };
    }

    /**
     * Получить значение по умолчанию для категории уведомлений
     */
    private boolean getDefaultCategoryEnabled(String category) {
        return switch (category.toLowerCase()) {
            case "marketing" -> false;
            default -> true;
        };
    }
}