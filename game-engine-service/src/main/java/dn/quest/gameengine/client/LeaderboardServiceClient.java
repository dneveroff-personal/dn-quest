package dn.quest.gameengine.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign клиент для интеграции с Leaderboard API в Statistics Service
 */
@FeignClient(name = "statistics-service", url = "${statistics.service.url:http://statistics-service:8087}")
public interface LeaderboardServiceClient {

    /**
     * Получить глобальный лидерборд пользователей
     */
    @GetMapping("/api/stats/leaderboard/global")
    ResponseEntity<Page<LeaderboardEntryDTO>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "all_time") String period,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    );

    /**
     * Получить лидерборд квестов
     */
    @GetMapping("/api/stats/leaderboard/quests")
    ResponseEntity<Page<LeaderboardEntryDTO>> getQuestLeaderboard(
            @RequestParam(defaultValue = "all_time") String period,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "rating") String metric,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    );

    /**
     * Получить лидерборд команд
     */
    @GetMapping("/api/stats/leaderboard/teams")
    ResponseEntity<Page<LeaderboardEntryDTO>> getTeamLeaderboard(
            @RequestParam(defaultValue = "all_time") String period,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "rating") String metric,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    );

    /**
     * Получить позицию пользователя в лидерборде
     */
    @GetMapping("/api/stats/leaderboard/users/{userId}/position")
    ResponseEntity<Map<String, Object>> getUserLeaderboardPosition(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "all_time") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    /**
     * Получить позицию команды в лидерборде
     */
    @GetMapping("/api/stats/leaderboard/teams/{teamId}/position")
    ResponseEntity<Map<String, Object>> getTeamLeaderboardPosition(
            @PathVariable UUID teamId,
            @RequestParam(defaultValue = "all_time") String period,
            @RequestParam(defaultValue = "rating") String metric,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    /**
     * Получить окружение пользователя в лидерборде
     */
    @GetMapping("/api/stats/leaderboard/users/{userId}/surrounding")
    ResponseEntity<List<LeaderboardEntryDTO>> getUserSurroundingInLeaderboard(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "all_time") String period,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    /**
     * Получить историю позиций пользователя в лидерборде
     */
    @GetMapping("/api/stats/leaderboard/users/{userId}/history")
    ResponseEntity<Map<String, Object>> getUserLeaderboardHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    /**
     * DTO для записи лидерборда
     */
    @Setter
    @Getter
    class LeaderboardEntryDTO {
        private UUID id;
        private String leaderboardType;
        private String period;
        private LocalDate date;
        private String entityId;
        private String entityName;
        private Integer rank;
        private Integer previousRank;
        private Integer rankChange;
        private Double score;
        private Double previousScore;
        private Double scoreChange;
        private String category;
        private String tags;
        private String metrics;
        private String avatarUrl;
        private String profileUrl;
        private Integer achievementsCount;
        private Integer level;
        private Double progressPercentage;
        private String status;
        private Boolean isActive;
        private Integer participationsCount;
        private Integer winsCount;
        private Double winRate;
        private Double avgCompletionTime;
        private Integer ratingsCount;
        private Double avgRating;
        private Integer viewsCount;
        private Integer likesCount;
        private Integer commentsCount;
        private String metadata;
    }
}