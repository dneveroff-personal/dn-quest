package dn.quest.usermanagement.service.impl;

import dn.quest.usermanagement.dto.SettingsStatisticsSummaryDTO;
import dn.quest.usermanagement.dto.UpdateSettingsRequestDTO;
import dn.quest.usermanagement.dto.UserSettingsDTO;
import dn.quest.usermanagement.entity.UserSettings;
import dn.quest.usermanagement.exception.UserManagementExceptions.UserSettingsNotFoundException;
import dn.quest.usermanagement.repository.UserSettingsRepository;
import dn.quest.usermanagement.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления настройками пользователей
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    @Override
    public UserSettingsDTO createUserSettings(UUID userId) {
        log.info("Создание настроек пользователя: userId={}", userId);

        UserSettings settings = UserSettings.builder()
                .userId(userId)
                .profilePublic(true)
                .showEmail(false)
                .showRealName(false)
                .showLocation(true)
                .showWebsite(true)
                .showStatistics(true)
                .emailNotifications(true)
                .teamInvitations(true)
                .questReminders(true)
                .achievementNotifications(true)
                .friendRequests(true)
                .systemNotifications(true)
                .theme("light")
                .language("ru")
                .timezone("UTC")
                .dateFormat("dd.MM.yyyy")
                .timeFormat("24h")
                .autoJoinTeams(false)
                .showHints(true)
                .soundEffects(true)
                .music(false)
                .animations(true)
                .build();

        settings = userSettingsRepository.save(settings);
        log.info("Настройки пользователя созданы: id={}", settings.getId());

        return mapToDto(settings);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSettingsDTO> getUserSettingsByUserId(UUID userId) {
        log.debug("Получение настроек пользователя по userId: {}", userId);
        return userSettingsRepository.findByUserId(userId).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSettingsDTO> getUserSettingsById(Long id) {
        log.debug("Получение настроек пользователя по id: {}", id);
        return userSettingsRepository.findById(id).map(this::mapToDto);
    }

    @Override
    public UserSettingsDTO updateUserSettings(UUID userId, UpdateSettingsRequestDTO request) {
        log.info("Обновление настроек пользователя: userId={}", userId);

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElse(null);

        if (settings == null) {
            return createUserSettings(userId);
        }

        if (request.getProfilePublic() != null) {
            settings.setProfilePublic(request.getProfilePublic());
        }
        if (request.getShowEmail() != null) {
            settings.setShowEmail(request.getShowEmail());
        }
        if (request.getShowRealName() != null) {
            settings.setShowRealName(request.getShowRealName());
        }
        if (request.getShowLocation() != null) {
            settings.setShowLocation(request.getShowLocation());
        }
        if (request.getShowWebsite() != null) {
            settings.setShowWebsite(request.getShowWebsite());
        }
        if (request.getShowStatistics() != null) {
            settings.setShowStatistics(request.getShowStatistics());
        }
        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getTeamInvitations() != null) {
            settings.setTeamInvitations(request.getTeamInvitations());
        }
        if (request.getQuestReminders() != null) {
            settings.setQuestReminders(request.getQuestReminders());
        }
        if (request.getAchievementNotifications() != null) {
            settings.setAchievementNotifications(request.getAchievementNotifications());
        }
        if (request.getFriendRequests() != null) {
            settings.setFriendRequests(request.getFriendRequests());
        }
        if (request.getSystemNotifications() != null) {
            settings.setSystemNotifications(request.getSystemNotifications());
        }
        if (request.getTheme() != null) {
            settings.setTheme(request.getTheme());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getTimezone() != null) {
            settings.setTimezone(request.getTimezone());
        }
        if (request.getDateFormat() != null) {
            settings.setDateFormat(request.getDateFormat());
        }
        if (request.getTimeFormat() != null) {
            settings.setTimeFormat(request.getTimeFormat());
        }
        if (request.getAutoJoinTeams() != null) {
            settings.setAutoJoinTeams(request.getAutoJoinTeams());
        }
        if (request.getShowHints() != null) {
            settings.setShowHints(request.getShowHints());
        }
        if (request.getSoundEffects() != null) {
            settings.setSoundEffects(request.getSoundEffects());
        }
        if (request.getMusic() != null) {
            settings.setMusic(request.getMusic());
        }
        if (request.getAnimations() != null) {
            settings.setAnimations(request.getAnimations());
        }

        settings = userSettingsRepository.save(settings);
        log.info("Настройки пользователя обновлены: userId={}", userId);

        return mapToDto(settings);
    }

    @Override
    public UserSettingsDTO updatePrivacySettings(UUID userId, Boolean profilePublic, Boolean showEmail,
                                                  Boolean showRealName, Boolean showLocation,
                                                  Boolean showWebsite, Boolean showStatistics) {
        log.info("Обновление настроек приватности: userId={}", userId);

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException("Настройки не найдены для userId: " + userId));

        if (profilePublic != null) {
            settings.setProfilePublic(profilePublic);
        }
        if (showEmail != null) {
            settings.setShowEmail(showEmail);
        }
        if (showRealName != null) {
            settings.setShowRealName(showRealName);
        }
        if (showLocation != null) {
            settings.setShowLocation(showLocation);
        }
        if (showWebsite != null) {
            settings.setShowWebsite(showWebsite);
        }
        if (showStatistics != null) {
            settings.setShowStatistics(showStatistics);
        }

        settings = userSettingsRepository.save(settings);
        return mapToDto(settings);
    }

    @Override
    public UserSettingsDTO updateNotificationSettings(UUID userId, Boolean emailNotifications,
                                                      Boolean teamInvitations, Boolean questReminders,
                                                      Boolean achievementNotifications,
                                                      Boolean friendRequests, Boolean systemNotifications) {
        log.info("Обновление настроек уведомлений: userId={}", userId);

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException("Настройки не найдены для userId: " + userId));

        if (emailNotifications != null) {
            settings.setEmailNotifications(emailNotifications);
        }
        if (teamInvitations != null) {
            settings.setTeamInvitations(teamInvitations);
        }
        if (questReminders != null) {
            settings.setQuestReminders(questReminders);
        }
        if (achievementNotifications != null) {
            settings.setAchievementNotifications(achievementNotifications);
        }
        if (friendRequests != null) {
            settings.setFriendRequests(friendRequests);
        }
        if (systemNotifications != null) {
            settings.setSystemNotifications(systemNotifications);
        }

        settings = userSettingsRepository.save(settings);
        return mapToDto(settings);
    }

    @Override
    public UserSettingsDTO updateInterfaceSettings(UUID userId, String theme, String language,
                                                    String timezone, String dateFormat, String timeFormat) {
        log.info("Обновление настроек интерфейса: userId={}", userId);

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException("Настройки не найдены для userId: " + userId));

        if (theme != null) {
            settings.setTheme(theme);
        }
        if (language != null) {
            settings.setLanguage(language);
        }
        if (timezone != null) {
            settings.setTimezone(timezone);
        }
        if (dateFormat != null) {
            settings.setDateFormat(dateFormat);
        }
        if (timeFormat != null) {
            settings.setTimeFormat(timeFormat);
        }

        settings = userSettingsRepository.save(settings);
        return mapToDto(settings);
    }

    @Override
    public UserSettingsDTO updateGameSettings(UUID userId, Boolean autoJoinTeams, Boolean showHints,
                                               Boolean soundEffects, Boolean music, Boolean animations) {
        log.info("Обновление игровых настроек: userId={}", userId);

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException("Настройки не найдены для userId: " + userId));

        if (autoJoinTeams != null) {
            settings.setAutoJoinTeams(autoJoinTeams);
        }
        if (showHints != null) {
            settings.setShowHints(showHints);
        }
        if (soundEffects != null) {
            settings.setSoundEffects(soundEffects);
        }
        if (music != null) {
            settings.setMusic(music);
        }
        if (animations != null) {
            settings.setAnimations(animations);
        }

        settings = userSettingsRepository.save(settings);
        return mapToDto(settings);
    }

    @Override
    public UserSettingsDTO resetToDefaults(UUID userId) {
        log.info("Сброс настроек к значениям по умолчанию: userId={}", userId);

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException("Настройки не найдены для userId: " + userId));

        settings.applyDefaults();
        settings = userSettingsRepository.save(settings);

        log.info("Настройки сброшены к значениям по умолчанию: userId={}", userId);
        return mapToDto(settings);
    }

    @Override
    public void deleteUserSettings(UUID userId) {
        log.info("Удаление настроек пользователя: userId={}", userId);
        userSettingsRepository.findByUserId(userId)
                .ifPresent(settings -> userSettingsRepository.delete(settings));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSettingsDTO> getUsersWithPublicProfiles() {
        log.debug("Получение пользователей с публичными профилями");
        return userSettingsRepository.findByProfilePublic(true)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSettingsDTO> getUsersWithEmailNotifications() {
        log.debug("Получение пользователей с включенными email уведомлениями");
        return userSettingsRepository.findByEmailNotifications(true)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSettingsDTO> getUsersForNotificationType(String notificationType) {
        log.debug("Получение пользователей для типа уведомлений: {}", notificationType);
        return switch (notificationType.toLowerCase()) {
            case "email" -> userSettingsRepository.findByEmailNotifications(true)
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            case "team_invitations" -> userSettingsRepository.findByTeamInvitations(true)
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            case "quest_reminders" -> userSettingsRepository.findByQuestReminders(true)
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            case "achievement_notifications" -> userSettingsRepository.findByAchievementNotifications(true)
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            case "friend_requests" -> userSettingsRepository.findByFriendRequests(true)
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            case "system_notifications" -> userSettingsRepository.findBySystemNotifications(true)
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            default -> List.of();
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSettingsDTO> getUsersByTheme(String theme) {
        log.debug("Получение пользователей с темой оформления: {}", theme);
        return userSettingsRepository.findByTheme(theme)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSettingsDTO> getUsersByLanguage(String language) {
        log.debug("Получение пользователей с языком: {}", language);
        return userSettingsRepository.findByLanguage(language)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSettingsDTO> getUsersByTimezone(String timezone) {
        log.debug("Получение пользователей с часовым поясом: {}", timezone);
        return userSettingsRepository.findByTimezone(timezone)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        log.debug("Проверка существования настроек для userId: {}", userId);
        return userSettingsRepository.existsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public SettingsStatisticsSummaryDTO getSettingsStatistics() {
        log.debug("Получение статистики по настройкам пользователей");

        long totalUsers = userSettingsRepository.count();
        long publicProfiles = userSettingsRepository.findByProfilePublic(true).size();
        long emailNotifications = userSettingsRepository.findByEmailNotifications(true).size();

        return SettingsStatisticsSummaryDTO.builder()
                .totalUsers(totalUsers)
                .publicProfiles(publicProfiles)
                .emailNotificationsEnabled(emailNotifications)
                .build();
    }

    /**
     * Преобразование сущности в DTO
     */
    private UserSettingsDTO mapToDto(UserSettings settings) {
        return UserSettingsDTO.builder()
                .id(settings.getId())
                .userId(settings.getUserId())
                .profilePublic(settings.getProfilePublic())
                .showEmail(settings.getShowEmail())
                .showRealName(settings.getShowRealName())
                .showLocation(settings.getShowLocation())
                .showWebsite(settings.getShowWebsite())
                .showStatistics(settings.getShowStatistics())
                .emailNotifications(settings.getEmailNotifications())
                .teamInvitations(settings.getTeamInvitations())
                .questReminders(settings.getQuestReminders())
                .achievementNotifications(settings.getAchievementNotifications())
                .friendRequests(settings.getFriendRequests())
                .systemNotifications(settings.getSystemNotifications())
                .theme(settings.getTheme())
                .language(settings.getLanguage())
                .timezone(settings.getTimezone())
                .dateFormat(settings.getDateFormat())
                .timeFormat(settings.getTimeFormat())
                .autoJoinTeams(settings.getAutoJoinTeams())
                .showHints(settings.getShowHints())
                .soundEffects(settings.getSoundEffects())
                .music(settings.getMusic())
                .animations(settings.getAnimations())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}