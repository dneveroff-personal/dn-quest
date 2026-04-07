package dn.quest.teammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для статистики команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatisticsDTO {

    private UUID id;
    private UUID teamId;
    private Integer totalMembers;
    private Integer activeMembers;
    private Long totalInvitationsSent;
    private Long totalInvitationsAccepted;
    private Long totalInvitationsDeclined;
    private Long totalGamesPlayed;
    private Long totalGamesWon;
    private Long totalGamesLost;
    private Long totalQuestsCompleted;
    private Long totalScore;
    private Double averageScore;
    private Double rating;
    private Integer rank;
    private Double winRate;
    private Instant lastActivityAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Получить процент принятия приглашений
     */
    public Double getInvitationAcceptanceRate() {
        if (totalInvitationsSent == null || totalInvitationsSent == 0) {
            return 0.0;
        }
        long accepted = totalInvitationsAccepted != null ? totalInvitationsAccepted : 0L;
        return (double) accepted / totalInvitationsSent * 100.0;
    }

    /**
     * Получить процент отклонения приглашений
     */
    public Double getInvitationDeclineRate() {
        if (totalInvitationsSent == null || totalInvitationsSent == 0) {
            return 0.0;
        }
        long declined = totalInvitationsDeclined != null ? totalInvitationsDeclined : 0L;
        return (double) declined / totalInvitationsSent * 100.0;
    }

    /**
     * Получить процент неактивных участников
     */
    public Double getInactiveMemberRate() {
        if (totalMembers == null || totalMembers == 0) {
            return 0.0;
        }
        int active = activeMembers != null ? activeMembers : 0;
        return (double) (totalMembers - active) / totalMembers * 100.0;
    }

    /**
     * Получить категорию рейтинга
     */
    public String getRatingCategory() {
        if (rating == null) {
            return "Неизвестно";
        }
        
        if (rating < 1000) {
            return "Новичок";
        } else if (rating < 1200) {
            return "Любитель";
        } else if (rating < 1400) {
            return "Продвинутый";
        } else if (rating < 1600) {
            return "Эксперт";
        } else if (rating < 1800) {
            return "Мастер";
        } else {
            return "Грандмастер";
        }
    }

    /**
     * Получить цвет рейтинга
     */
    public String getRatingColor() {
        if (rating == null) {
            return "#9CA3AF"; // Серый
        }
        
        if (rating < 1000) {
            return "#9CA3AF"; // Серый
        } else if (rating < 1200) {
            return "#10B981"; // Зеленый
        } else if (rating < 1400) {
            return "#3B82F6"; // Синий
        } else if (rating < 1600) {
            return "#8B5CF6"; // Фиолетовый
        } else if (rating < 1800) {
            return "#F59E0B"; // Оранжевый
        } else {
            return "#EF4444"; // Красный
        }
    }

    /**
     * Получить статус активности
     */
    public String getActivityStatus() {
        if (lastActivityAt == null) {
            return "Никогда";
        }
        
        Instant now = Instant.now();
        long hours = java.time.Duration.between(lastActivityAt, now).toHours();
        
        if (hours < 1) {
            return "Активна";
        } else if (hours < 24) {
            return "Недавно";
        } else if (hours < 168) { // 7 дней
            return "Редко";
        } else {
            return "Неактивна";
        }
    }

    /**
     * Получить описание винрейта
     */
    public String getWinRateDescription() {
        if (winRate == null) {
            return "Нет данных";
        }
        
        if (winRate >= 80) {
            return "Отличный";
        } else if (winRate >= 60) {
            return "Хороший";
        } else if (winRate >= 40) {
            return "Средний";
        } else if (winRate >= 20) {
            return "Низкий";
        } else {
            return "Очень низкий";
        }
    }

    /**
     * Получить цвет винрейта
     */
    public String getWinRateColor() {
        if (winRate == null) {
            return "#9CA3AF"; // Серый
        }
        
        if (winRate >= 80) {
            return "#10B981"; // Зеленый
        } else if (winRate >= 60) {
            return "#3B82F6"; // Синий
        } else if (winRate >= 40) {
            return "#F59E0B"; // Оранжевый
        } else {
            return "#EF4444"; // Красный
        }
    }

    /**
     * Получить форматированный рейтинг
     */
    public String getFormattedRating() {
        if (rating == null) {
            return "0";
        }
        return String.format("%.0f", rating);
    }

    /**
     * Получить форматированный винрейт
     */
    public String getFormattedWinRate() {
        if (winRate == null) {
            return "0%";
        }
        return String.format("%.1f%%", winRate);
    }

    /**
     * Получить форматированный средний счет
     */
    public String getFormattedAverageScore() {
        if (averageScore == null) {
            return "0";
        }
        return String.format("%.1f", averageScore);
    }

    /**
     * Проверить, есть ли у команды достаточная статистика
     */
    public boolean hasEnoughStatistics() {
        return (totalGamesPlayed != null && totalGamesPlayed > 0) ||
               (totalQuestsCompleted != null && totalQuestsCompleted > 0);
    }

    /**
     * Получить уровень опыта команды
     */
    public String getExperienceLevel() {
        long totalActivities = (totalGamesPlayed != null ? totalGamesPlayed : 0L) + 
                              (totalQuestsCompleted != null ? totalQuestsCompleted : 0L);
        
        if (totalActivities < 10) {
            return "Начинающая";
        } else if (totalActivities < 50) {
            return "Опытная";
        } else if (totalActivities < 100) {
            return "Продвинутая";
        } else {
            return "Ветеранская";
        }
    }
}