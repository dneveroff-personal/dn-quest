package dn.quest.questmanagement.client;

import dn.quest.shared.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * Feign клиент для интеграции с User Management Service
 */
@FeignClient(name = "user-management-service", url = "${user-management.service.url:http://user-management-service:8082}")
public interface UserManagementServiceClient {

    /**
     * Получить информацию о пользователе по ID
     */
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    /**
     * Получить информацию о пользователе по username
     */
    @GetMapping("/api/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    /**
     * Получить пользователей по списку ID
     */
    @GetMapping("/api/users/batch")
    List<UserDTO> getUsersByIds(@RequestHeader("X-User-Ids") List<Long> userIds);

    /**
     * Получить авторов квестов
     */
    @GetMapping("/api/users/authors")
    List<UserDTO> getQuestAuthors();

    /**
     * Проверить, является ли пользователь автором квестов
     */
    @GetMapping("/api/users/{id}/is-author")
    Boolean isQuestAuthor(@PathVariable("id") Long userId);

    /**
     * Получить статистику пользователя
     */
    @GetMapping("/api/users/{id}/statistics")
    UserStatisticsDTO getUserStatistics(@PathVariable("id") Long userId);

    /**
     * DTO для статистики пользователя
     */
    class UserStatisticsDTO {
        private Long totalQuests;
        private Long publishedQuests;
        private Long activeQuests;
        private Long totalParticipants;
        private Double averageRating;

        // Getters and setters
        public Long getTotalQuests() { return totalQuests; }
        public void setTotalQuests(Long totalQuests) { this.totalQuests = totalQuests; }

        public Long getPublishedQuests() { return publishedQuests; }
        public void setPublishedQuests(Long publishedQuests) { this.publishedQuests = publishedQuests; }

        public Long getActiveQuests() { return activeQuests; }
        public void setActiveQuests(Long activeQuests) { this.activeQuests = activeQuests; }

        public Long getTotalParticipants() { return totalParticipants; }
        public void setTotalParticipants(Long totalParticipants) { this.totalParticipants = totalParticipants; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    }
}