package dn.quest.notification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.base.AbstractIntegrationTestBase;
import dn.quest.shared.util.EnhancedTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для Notification Service
 */
class NotificationIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для уведомлений
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSendNotification_Success() throws Exception {
        // Given
        var notificationRequest = new java.util.HashMap<String, Object>();
        notificationRequest.put("userId", 1L);
        notificationRequest.put("type", "QUEST_COMPLETED");
        notificationRequest.put("title", "Quest Completed!");
        notificationRequest.put("message", "Congratulations! You have completed the quest.");
        notificationRequest.put("data", Map.of("questId", 1L, "score", 100));

        // When & Then
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.type").value("QUEST_COMPLETED"))
                .andExpect(jsonPath("$.title").value("Quest Completed!"))
                .andExpect(jsonPath("$.message").value("Congratulations! You have completed the quest."))
                .andExpect(jsonPath("$.data.questId").value(1))
                .andExpect(jsonPath("$.data.score").value(100))
                .andExpect(jsonPath("$.read").value(false))
                .andExpect(jsonPath("$.createdAt").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSendNotification_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        var invalidNotification = new java.util.HashMap<String, Object>();
        invalidNotification.put("userId", null); // Отсутствует userId
        invalidNotification.put("type", "QUEST_COMPLETED");
        invalidNotification.put("title", ""); // Пустой заголовок
        invalidNotification.put("message", "Test message");

        // When & Then
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNotification)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSendBulkNotification_Success() throws Exception {
        // Given
        var bulkRequest = new java.util.HashMap<String, Object>();
        bulkRequest.put("userIds", List.of(1L, 2L, 3L));
        bulkRequest.put("type", "SYSTEM_ANNOUNCEMENT");
        bulkRequest.put("title", "System Maintenance");
        bulkRequest.put("message", "System will be under maintenance from 2 AM to 4 AM.");
        bulkRequest.put("priority", "HIGH");

        // When & Then
        mockMvc.perform(post("/api/notifications/send-bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sentNotifications").isArray())
                .andExpect(jsonPath("$.sentNotifications").hasJsonPath(3))
                .andExpect(jsonPath("$.totalSent").value(3))
                .andExpect(jsonPath("$.totalFailed").value(0));

        // Проверка отправки событий в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetMyNotifications_Success() throws Exception {
        // Given - Сначала создаем несколько уведомлений
        for (int i = 0; i < 3; i++) {
            var notificationRequest = new java.util.HashMap<String, Object>();
            notificationRequest.put("userId", 1L);
            notificationRequest.put("type", "QUEST_UPDATE");
            notificationRequest.put("title", "Quest Update " + i);
            notificationRequest.put("message", "Quest has been updated " + i);

            mockMvc.perform(post("/api/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем уведомления пользователя
        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetMyNotifications_UnreadOnly_Success() throws Exception {
        // Given - Создаем уведомления и помечаем некоторые как прочитанные
        for (int i = 0; i < 2; i++) {
            var notificationRequest = new java.util.HashMap<String, Object>();
            notificationRequest.put("userId", 1L);
            notificationRequest.put("type", "ACHIEVEMENT_UNLOCKED");
            notificationRequest.put("title", "Achievement " + i);
            notificationRequest.put("message", "You unlocked an achievement " + i);

            String response = mockMvc.perform(post("/api/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long notificationId = objectMapper.readTree(response).get("id").asLong();
            
            // Помечаем первое уведомление как прочитанное
            if (i == 0) {
                mockMvc.perform(put("/api/notifications/" + notificationId + "/read"))
                        .andExpect(status().isOk());
            }
        }

        // When & Then - Получаем только непрочитанные уведомления
        mockMvc.perform(get("/api/notifications/my")
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testMarkNotificationAsRead_Success() throws Exception {
        // Given - Сначала создаем уведомление
        var notificationRequest = new java.util.HashMap<String, Object>();
        notificationRequest.put("userId", 1L);
        notificationRequest.put("type", "TEAM_INVITATION");
        notificationRequest.put("title", "Team Invitation");
        notificationRequest.put("message", "You have been invited to join a team.");

        String response = mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long notificationId = objectMapper.readTree(response).get("id").asLong();

        // When & Then - Помечаем уведомление как прочитанное
        mockMvc.perform(put("/api/notifications/" + notificationId + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId))
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testMarkAllNotificationsAsRead_Success() throws Exception {
        // Given - Создаем несколько уведомлений
        for (int i = 0; i < 3; i++) {
            var notificationRequest = new java.util.HashMap<String, Object>();
            notificationRequest.put("userId", 1L);
            notificationRequest.put("type", "SYSTEM_MESSAGE");
            notificationRequest.put("title", "System Message " + i);
            notificationRequest.put("message", "System notification " + i);

            mockMvc.perform(post("/api/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Помечаем все уведомления как прочитанные
        mockMvc.perform(put("/api/notifications/mark-all-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All notifications marked as read"))
                .andExpect(jsonPath("$.updatedCount").value(3));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testDeleteNotification_Success() throws Exception {
        // Given - Сначала создаем уведомление
        var notificationRequest = new java.util.HashMap<String, Object>();
        notificationRequest.put("userId", 1L);
        notificationRequest.put("type", "QUEST_REMINDER");
        notificationRequest.put("title", "Quest Reminder");
        notificationRequest.put("message", "Don't forget to complete your quest!");

        String response = mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long notificationId = objectMapper.readTree(response).get("id").asLong();

        // When & Then - Удаляем уведомление
        mockMvc.perform(delete("/api/notifications/" + notificationId))
                .andExpect(status().isNoContent());

        // Проверяем, что уведомление удалено
        mockMvc.perform(get("/api/notifications/" + notificationId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetNotificationById_Success() throws Exception {
        // Given - Сначала создаем уведомление
        var notificationRequest = new java.util.HashMap<String, Object>();
        notificationRequest.put("userId", 1L);
        notificationRequest.put("type", "LEVEL_COMPLETED");
        notificationRequest.put("title", "Level Completed!");
        notificationRequest.put("message", "You have completed level 5!");
        notificationRequest.put("data", Map.of("level", 5, "experience", 500));

        String response = mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long notificationId = objectMapper.readTree(response).get("id").asLong();

        // When & Then - Получаем уведомление по ID
        mockMvc.perform(get("/api/notifications/" + notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId))
                .andExpect(jsonPath("$.type").value("LEVEL_COMPLETED"))
                .andExpect(jsonPath("$.title").value("Level Completed!"))
                .andExpect(jsonPath("$.data.level").value(5))
                .andExpect(jsonPath("$.data.experience").value(500));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetNotificationById_NotFound_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetNotificationsByType_Success() throws Exception {
        // Given - Создаем уведомления разных типов
        var questNotification = new java.util.HashMap<String, Object>();
        questNotification.put("userId", 1L);
        questNotification.put("type", "QUEST_COMPLETED");
        questNotification.put("title", "Quest Completed");
        questNotification.put("message", "Quest completed successfully");

        var achievementNotification = new java.util.HashMap<String, Object>();
        achievementNotification.put("userId", 1L);
        achievementNotification.put("type", "ACHIEVEMENT_UNLOCKED");
        achievementNotification.put("title", "Achievement Unlocked");
        achievementNotification.put("message", "New achievement unlocked");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questNotification)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(achievementNotification)))
                .andExpect(status().isCreated());

        // When & Then - Получаем уведомления по типу
        mockMvc.perform(get("/api/notifications/type/QUEST_COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("QUEST_COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUnreadCount_Success() throws Exception {
        // Given - Создаем несколько непрочитанных уведомлений
        for (int i = 0; i < 3; i++) {
            var notificationRequest = new java.util.HashMap<String, Object>();
            notificationRequest.put("userId", 1L);
            notificationRequest.put("type", "SYSTEM_UPDATE");
            notificationRequest.put("title", "System Update " + i);
            notificationRequest.put("message", "System has been updated " + i);

            mockMvc.perform(post("/api/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем количество непрочитанных уведомлений
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllNotifications_Admin_Success() throws Exception {
        // Given - Создаем уведомления для разных пользователей
        for (int i = 0; i < 3; i++) {
            var notificationRequest = new java.util.HashMap<String, Object>();
            notificationRequest.put("userId", (long) (i + 1));
            notificationRequest.put("type", "ADMIN_MESSAGE");
            notificationRequest.put("title", "Admin Message " + i);
            notificationRequest.put("message", "Admin notification " + i);

            mockMvc.perform(post("/api/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Администратор получает все уведомления
        mockMvc.perform(get("/api/notifications/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetAllNotifications_Player_Forbidden() throws Exception {
        // When & Then - Обычный пользователь не может получить все уведомления
        mockMvc.perform(get("/api/notifications/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testSendSystemNotification_Success() throws Exception {
        // Given
        var systemNotification = new java.util.HashMap<String, Object>();
        systemNotification.put("title", "System Maintenance");
        systemNotification.put("message", "System will be under maintenance tonight");
        systemNotification.put("priority", "HIGH");
        systemNotification.put("targetRoles", List.of("PLAYER", "MODERATOR"));

        // When & Then
        mockMvc.perform(post("/api/notifications/admin/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemNotification)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("System notification sent successfully"))
                .andExpect(jsonPath("$.recipientsCount").exists())
                .andExpect(jsonPath("$.notificationId").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSubscribeToNotifications_Success() throws Exception {
        // Given
        var subscriptionRequest = new java.util.HashMap<String, Object>();
        subscriptionRequest.put("types", List.of("QUEST_COMPLETED", "ACHIEVEMENT_UNLOCKED"));
        subscriptionRequest.put("channels", List.of("EMAIL", "PUSH"));

        // When & Then
        mockMvc.perform(post("/api/notifications/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subscription preferences updated"))
                .andExpect(jsonPath("$.subscribedTypes").isArray())
                .andExpect(jsonPath("$.subscribedTypes[0]").value("QUEST_COMPLETED"))
                .andExpect(jsonPath("$.subscribedChannels").isArray())
                .andExpect(jsonPath("$.subscribedChannels[0]").value("EMAIL"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUnsubscribeFromNotifications_Success() throws Exception {
        // Given
        var unsubscribeRequest = new java.util.HashMap<String, Object>();
        unsubscribeRequest.put("types", List.of("SYSTEM_MESSAGE"));
        unsubscribeRequest.put("channels", List.of("EMAIL"));

        // When & Then
        mockMvc.perform(post("/api/notifications/unsubscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unsubscribeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unsubscription successful"))
                .andExpect(jsonPath("$.unsubscribedTypes").isArray())
                .andExpect(jsonPath("$.unsubscribedTypes[0]").value("SYSTEM_MESSAGE"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetNotificationPreferences_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.subscribedTypes").isArray())
                .andExpect(jsonPath("$.subscribedChannels").isArray())
                .andExpect(jsonPath("$.quietHours").exists())
                .andExpect(jsonPath("$.frequency").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateNotificationPreferences_Success() throws Exception {
        // Given
        var preferencesRequest = new java.util.HashMap<String, Object>();
        preferencesRequest.put("subscribedTypes", List.of("QUEST_COMPLETED", "TEAM_INVITATION"));
        preferencesRequest.put("subscribedChannels", List.of("PUSH", "IN_APP"));
        preferencesRequest.put("quietHours", Map.of("start", "22:00", "end", "08:00"));
        preferencesRequest.put("frequency", "DAILY");

        // When & Then
        mockMvc.perform(put("/api/notifications/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferencesRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification preferences updated successfully"))
                .andExpect(jsonPath("$.subscribedTypes").isArray())
                .andExpect(jsonPath("$.subscribedTypes[0]").value("QUEST_COMPLETED"))
                .andExpect(jsonPath("$.frequency").value("DAILY"));
    }
}