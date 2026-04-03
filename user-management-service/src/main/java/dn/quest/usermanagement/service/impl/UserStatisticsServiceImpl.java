package dn.quest.usermanagement.service.impl;

import dn.quest.usermanagement.dto.UserStatisticsDTO;
import dn.quest.usermanagement.entity.UserStatistics;
import dn.quest.usermanagement.repository.UserStatisticsRepository;
import dn.quest.usermanagement.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления статистикой пользователей
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserStatisticsRepository userStatisticsRepository;

    @Override
    @Transactional
    public UserStatisticsDTO createUserStatistics(UUID userId) {
        log.info("Creating user statistics for user ID: {}", userId);

        if (userStatisticsRepository.existsByUserId(userId)) {
            log.warn("User statistics already exist for user ID: {}", userId);
            return getUserStatisticsByUserId(userId).orElseThrow();
        }

        UserStatistics statistics = UserStatistics.builder()
                .userId(userId)
                .build();

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("User statistics created successfully for user ID: {}", userId);

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserStatisticsDTO> getUserStatisticsByUserId(UUID userId) {
        return userStatisticsRepository.findByUserId(userId)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserStatisticsDTO> getUserStatisticsById(Long id) {
        return userStatisticsRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public UserStatisticsDTO addExperience(UUID userId, Long experience) {
        log.info("Adding {} experience to user ID: {}", experience, userId);

        UserStatistics statistics = getOrCreateStatistics(userId);
        statistics.addExperience(experience);

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Experience added successfully. New level: {}, experience: {}",
                savedStatistics.getLevel(), savedStatistics.getExperiencePoints());

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO addScore(UUID userId, Long score) {
        log.info("Adding {} score to user ID: {}", score, userId);

        UserStatistics statistics = getOrCreateStatistics(userId);
        statistics.addScore(score);

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Score added successfully. Total score: {}", savedStatistics.getTotalScore());

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO updateQuestStatistics(UUID userId, Boolean completed, Long playtimeMinutes) {
        log.info("Updating quest statistics for user ID: {}. Completed: {}, playtime: {} minutes",
                userId, completed, playtimeMinutes);

        UserStatistics statistics = getOrCreateStatistics(userId);

        if (completed != null && completed) {
            statistics.incrementQuestsCompleted();
        }

        if (playtimeMinutes != null && playtimeMinutes > 0) {
            statistics.addPlaytime(playtimeMinutes);
        }

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Quest statistics updated. Quests completed: {}, total playtime: {} minutes",
                savedStatistics.getQuestsCompleted(), savedStatistics.getTotalPlaytimeMinutes());

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO updateLevelStatistics(UUID userId, Boolean levelCompleted,
                                                    Boolean codeSolved, Boolean hintUsed, Boolean attemptMade) {
        log.info("Updating level statistics for user ID: {}. Level completed: {}, code solved: {}, hint used: {}, attempt made: {}",
                userId, levelCompleted, codeSolved, hintUsed, attemptMade);

        UserStatistics statistics = getOrCreateStatistics(userId);

        if (levelCompleted != null && levelCompleted) {
            statistics.setLevelsCompleted(statistics.getLevelsCompleted() + 1);
        }

        if (codeSolved != null && codeSolved) {
            statistics.setCodesSolved(statistics.getCodesSolved() + 1);
        }

        if (hintUsed != null && hintUsed) {
            statistics.setHintsUsed(statistics.getHintsUsed() + 1);
        }

        if (attemptMade != null && attemptMade) {
            statistics.setAttemptsMade(statistics.getAttemptsMade() + 1);
        }

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Level statistics updated. Levels completed: {}, codes solved: {}",
                savedStatistics.getLevelsCompleted(), savedStatistics.getCodesSolved());

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO updateTeamStatistics(UUID userId, String actionType) {
        log.info("Updating team statistics for user ID: {}. Action type: {}", userId, actionType);

        UserStatistics statistics = getOrCreateStatistics(userId);

        switch (actionType.toLowerCase()) {
            case "join":
                statistics.setTeamsJoined(statistics.getTeamsJoined() + 1);
                break;
            case "create":
                statistics.setTeamsCreated(statistics.getTeamsCreated() + 1);
                break;
            case "lead":
                statistics.setTeamsLed(statistics.getTeamsLed() + 1);
                break;
            case "invite_sent":
                statistics.setInvitationsSent(statistics.getInvitationsSent() + 1);
                break;
            case "invite_received":
                statistics.setInvitationsReceived(statistics.getInvitationsReceived() + 1);
                break;
            default:
                log.warn("Unknown team action type: {}", actionType);
        }

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Team statistics updated");

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO updateAchievementStatistics(UUID userId, String achievementType) {
        log.info("Updating achievement statistics for user ID: {}. Achievement type: {}", userId, achievementType);

        UserStatistics statistics = getOrCreateStatistics(userId);

        statistics.setAchievementsUnlocked(statistics.getAchievementsUnlocked() + 1);

        switch (achievementType.toLowerCase()) {
            case "rare":
                statistics.setRareAchievements(statistics.getRareAchievements() + 1);
                break;
            case "legendary":
                statistics.setLegendaryAchievements(statistics.getLegendaryAchievements() + 1);
                break;
            default:
                // Regular achievement, no additional counter
                break;
        }

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Achievement statistics updated. Total achievements: {}",
                savedStatistics.getAchievementsUnlocked());

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO updateLoginStatistics(UUID userId) {
        log.info("Updating login statistics for user ID: {}", userId);

        UserStatistics statistics = getOrCreateStatistics(userId);
        statistics.incrementLoginCount();

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Login statistics updated. Login count: {}", savedStatistics.getLoginCount());

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO updateStreakStatistics(UUID userId) {
        log.info("Updating streak statistics for user ID: {}", userId);

        UserStatistics statistics = getOrCreateStatistics(userId);

        Instant now = Instant.now();
        Instant lastLogin = statistics.getLastLoginAt();

        if (lastLogin != null) {
            long daysBetween = ChronoUnit.DAYS.between(lastLogin, now);

            if (daysBetween == 1) {
                // Consecutive day
                statistics.setCurrentStreakDays(statistics.getCurrentStreakDays() + 1);
                if (statistics.getCurrentStreakDays() > statistics.getLongestStreakDays()) {
                    statistics.setLongestStreakDays(statistics.getCurrentStreakDays());
                }
            } else if (daysBetween > 1) {
                // Streak broken
                statistics.setCurrentStreakDays(1);
            }
            // If daysBetween == 0, same day, no change
        } else {
            // First login
            statistics.setCurrentStreakDays(1);
            statistics.setLongestStreakDays(1);
        }

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Streak statistics updated. Current streak: {}, longest streak: {}",
                savedStatistics.getCurrentStreakDays(), savedStatistics.getLongestStreakDays());

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public UserStatisticsDTO updateLastActivity(UUID userId) {
        log.info("Updating last activity for user ID: {}", userId);

        UserStatistics statistics = getOrCreateStatistics(userId);
        statistics.updateLastActivity();

        UserStatistics savedStatistics = userStatisticsRepository.save(statistics);
        log.info("Last activity updated");

        return convertToDTO(savedStatistics);
    }

    @Override
    @Transactional
    public void deleteUserStatistics(UUID userId) {
        log.info("Deleting user statistics for user ID: {}", userId);

        userStatisticsRepository.findByUserId(userId)
                .ifPresent(userStatisticsRepository::delete);

        log.info("User statistics deleted for user ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStatisticsDTO> getTopUsersByScore(Pageable pageable) {
        return userStatisticsRepository.findTopByTotalScore(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStatisticsDTO> getTopUsersByLevel(Pageable pageable) {
        return userStatisticsRepository.findTopByLevel(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStatisticsDTO> getTopUsersByQuests(Pageable pageable) {
        return userStatisticsRepository.findTopByQuestsCompleted(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStatisticsDTO> getTopUsersByCodes(Pageable pageable) {
        return userStatisticsRepository.findTopByCodesSolved(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStatisticsDTO> getTopUsersByAchievements(Pageable pageable) {
        return userStatisticsRepository.findTopByAchievements(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatisticsDTO> getUsersByLevel(Integer minLevel, Integer maxLevel) {
        return userStatisticsRepository.findByLevelBetween(minLevel, maxLevel)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatisticsDTO> getUsersByScoreRange(Long minScore, Long maxScore) {
        return userStatisticsRepository.findByTotalScoreBetween(minScore, maxScore)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatisticsDTO> getActivePlayers(Long minScore, Integer minQuests, Instant since) {
        return userStatisticsRepository.findActivePlayers(minScore, minQuests, since)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatisticsDTO> getNewPlayers(Instant since) {
        return userStatisticsRepository.findNewPlayers(since)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatisticsDTO> getVeteranPlayers(Instant before) {
        return userStatisticsRepository.findVeteranPlayers(before)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatisticsDTO> getPlayersWithStreak(Integer minDays) {
        return userStatisticsRepository.findPlayersWithStreak(minDays)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatisticsDTO> getFrequentPlayers(Integer minLogins) {
        return userStatisticsRepository.findFrequentPlayers(minLogins)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserRankByScore(UUID userId) {
        UserStatistics statistics = userStatisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User statistics not found for user ID: " + userId));

        return userStatisticsRepository.getRankByScore(statistics.getTotalScore());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserRankByLevel(UUID userId) {
        UserStatistics statistics = userStatisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User statistics not found for user ID: " + userId));

        return userStatisticsRepository.getRankByLevel(statistics.getLevel(), statistics.getExperiencePoints());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return userStatisticsRepository.existsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public dn.quest.usermanagement.dto.GlobalStatisticsSummaryDTO getGlobalStatistics() {
        log.info("Getting global statistics");

        long totalUsers = userStatisticsRepository.count();
        long activeUsers = userStatisticsRepository.findByLastActivityAtAfter(
                Instant.now().minus(30, ChronoUnit.DAYS)).size();

        Double averageScore = userStatisticsRepository.getAverageScore();
        Double averageLevel = userStatisticsRepository.getAverageLevel();
        Double averageQuestsCompleted = userStatisticsRepository.getAverageQuestsCompleted();
        Long totalScore = userStatisticsRepository.getTotalScore();
        Long totalPlaytimeMinutes = userStatisticsRepository.getTotalPlaytimeMinutes();

        // Users by level range
        Map<String, Long> usersByLevelRange = new HashMap<>();
        usersByLevelRange.put("1-10", userStatisticsRepository.countByLevelAtLeast(1) -
                userStatisticsRepository.countByLevelAtLeast(11));
        usersByLevelRange.put("11-25", userStatisticsRepository.countByLevelAtLeast(11) -
                userStatisticsRepository.countByLevelAtLeast(26));
        usersByLevelRange.put("26-50", userStatisticsRepository.countByLevelAtLeast(26) -
                userStatisticsRepository.countByLevelAtLeast(51));
        usersByLevelRange.put("51+", userStatisticsRepository.countByLevelAtLeast(51));

        // Users by quest range
        Map<String, Long> usersByQuestRange = new HashMap<>();
        List<UserStatistics> allStats = userStatisticsRepository.findAll();
        usersByQuestRange.put("0", allStats.stream().filter(s -> s.getQuestsCompleted() == 0).count());
        usersByQuestRange.put("1-5", allStats.stream().filter(s -> s.getQuestsCompleted() >= 1 && s.getQuestsCompleted() <= 5).count());
        usersByQuestRange.put("6-20", allStats.stream().filter(s -> s.getQuestsCompleted() >= 6 && s.getQuestsCompleted() <= 20).count());
        usersByQuestRange.put("21+", allStats.stream().filter(s -> s.getQuestsCompleted() > 20).count());

        return dn.quest.usermanagement.dto.GlobalStatisticsSummaryDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .averageScore(averageScore != null ? averageScore : 0.0)
                .averageLevel(averageLevel != null ? averageLevel : 0.0)
                .averageQuestsCompleted(averageQuestsCompleted != null ? averageQuestsCompleted : 0.0)
                .totalScore(totalScore != null ? totalScore : 0L)
                .totalPlaytimeMinutes(totalPlaytimeMinutes != null ? totalPlaytimeMinutes : 0L)
                .usersByLevelRange(usersByLevelRange)
                .usersByQuestRange(usersByQuestRange)
                .build();
    }

    @Override
    @Transactional
    public void updateGameSessionStatistics(UUID userId, Long sessionId, String status) {
        log.info("Updating game session statistics for user ID: {}. Session ID: {}, status: {}", userId, sessionId, status);

        UserStatistics statistics = getOrCreateStatistics(userId);
        
        // Update last activity
        statistics.updateLastActivity();
        
        userStatisticsRepository.save(statistics);
        log.info("Game session statistics updated");
    }

    @Override
    @Transactional
    public void updateLevelCompletionStatistics(UUID userId, Integer levelNumber) {
        log.info("Updating level completion statistics for user ID: {}. Level number: {}", userId, levelNumber);

        UserStatistics statistics = getOrCreateStatistics(userId);
        statistics.setLevelsCompleted(statistics.getLevelsCompleted() + 1);
        
        userStatisticsRepository.save(statistics);
        log.info("Level completion statistics updated. Total levels completed: {}", statistics.getLevelsCompleted());
    }

    @Override
    @Transactional
    public void updateFileStatistics(UUID userId, Long fileId, String action) {
        log.info("Updating file statistics for user ID: {}. File ID: {}, action: {}", userId, fileId, action);

        UserStatistics statistics = getOrCreateStatistics(userId);
        
        // Update last activity
        statistics.updateLastActivity();
        
        userStatisticsRepository.save(statistics);
        log.info("File statistics updated");
    }

    // Helper methods

    private UserStatistics getOrCreateStatistics(UUID userId) {
        return userStatisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new statistics for user ID: {}", userId);
                    UserStatistics newStats = UserStatistics.builder()
                            .userId(userId)
                            .build();
                    return userStatisticsRepository.save(newStats);
                });
    }

    private UserStatisticsDTO convertToDTO(UserStatistics statistics) {
        UserStatisticsDTO dto = UserStatisticsDTO.builder()
                .id(statistics.getId())
                .userId(statistics.getUserId())
                .totalScore(statistics.getTotalScore())
                .level(statistics.getLevel())
                .experiencePoints(statistics.getExperiencePoints())
                .experienceToNextLevel(statistics.getExperienceToNextLevel())
                .questsCompleted(statistics.getQuestsCompleted())
                .questsStarted(statistics.getQuestsStarted())
                .questsAbandoned(statistics.getQuestsAbandoned())
                .totalPlaytimeMinutes(statistics.getTotalPlaytimeMinutes())
                .levelsCompleted(statistics.getLevelsCompleted())
                .codesSolved(statistics.getCodesSolved())
                .hintsUsed(statistics.getHintsUsed())
                .attemptsMade(statistics.getAttemptsMade())
                .teamsJoined(statistics.getTeamsJoined())
                .teamsCreated(statistics.getTeamsCreated())
                .teamsLed(statistics.getTeamsLed())
                .invitationsSent(statistics.getInvitationsSent())
                .invitationsReceived(statistics.getInvitationsReceived())
                .achievementsUnlocked(statistics.getAchievementsUnlocked())
                .rareAchievements(statistics.getRareAchievements())
                .legendaryAchievements(statistics.getLegendaryAchievements())
                .loginCount(statistics.getLoginCount())
                .currentStreakDays(statistics.getCurrentStreakDays())
                .longestStreakDays(statistics.getLongestStreakDays())
                .lastLoginAt(statistics.getLastLoginAt())
                .lastActivityAt(statistics.getLastActivityAt())
                .firstLoginAt(statistics.getFirstLoginAt())
                .createdAt(statistics.getCreatedAt())
                .updatedAt(statistics.getUpdatedAt())
                .build();

        // Calculate computed fields
        calculateComputedFields(dto, statistics);

        return dto;
    }

    private void calculateComputedFields(UserStatisticsDTO dto, UserStatistics statistics) {
        // Level progress
        if (statistics.getExperienceToNextLevel() > 0) {
            double progress = (statistics.getExperiencePoints().doubleValue() /
                    statistics.getExperienceToNextLevel().doubleValue()) * 100.0;
            dto.setLevelProgress(Math.round(progress * 100.0) / 100.0);
        }

        // Total playtime in hours
        if (statistics.getTotalPlaytimeMinutes() > 0) {
            double hours = statistics.getTotalPlaytimeMinutes().doubleValue() / 60.0;
            dto.setTotalPlaytimeHours(Math.round(hours * 100.0) / 100.0);
        }

        // Average time per quest
        if (statistics.getQuestsCompleted() > 0 && statistics.getTotalPlaytimeMinutes() > 0) {
            double avgTime = statistics.getTotalPlaytimeMinutes().doubleValue() /
                    statistics.getQuestsCompleted().doubleValue();
            dto.setAverageTimePerQuest(Math.round(avgTime * 100.0) / 100.0);
        }

        // Average time per level
        if (statistics.getLevelsCompleted() > 0 && statistics.getTotalPlaytimeMinutes() > 0) {
            double avgTime = statistics.getTotalPlaytimeMinutes().doubleValue() /
                    statistics.getLevelsCompleted().doubleValue();
            dto.setAverageTimePerLevel(Math.round(avgTime * 100.0) / 100.0);
        }

        // Quest completion rate
        int totalQuests = statistics.getQuestsStarted();
        if (totalQuests > 0) {
            double rate = (statistics.getQuestsCompleted().doubleValue() / totalQuests) * 100.0;
            dto.setQuestCompletionRate(Math.round(rate * 100.0) / 100.0);
        }

        // Code success rate
        if (statistics.getAttemptsMade() > 0) {
            double rate = (statistics.getCodesSolved().doubleValue() /
                    statistics.getAttemptsMade().doubleValue()) * 100.0;
            dto.setCodeSuccessRate(Math.round(rate * 100.0) / 100.0);
        }

        // Days since first login
        if (statistics.getFirstLoginAt() != null) {
            long days = ChronoUnit.DAYS.between(statistics.getFirstLoginAt(), Instant.now());
            dto.setDaysSinceFirstLogin((int) days);

            // Average logins per day
            if (days > 0) {
                double avgLogins = statistics.getLoginCount().doubleValue() / days;
                dto.setAverageLoginsPerDay(Math.round(avgLogins * 100.0) / 100.0);
            }
        }
    }
}
