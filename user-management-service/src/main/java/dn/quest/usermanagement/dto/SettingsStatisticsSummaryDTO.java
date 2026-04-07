package dn.quest.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для сводной статистики настроек
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сводная статистика настроек пользователей")
public class SettingsStatisticsSummaryDTO {

    @Schema(description = "Общее количество пользователей с настройками", example = "1500")
    private long totalUsers;

    @Schema(description = "Количество пользователей с публичным профилем", example = "1200")
    private long publicProfiles;

    @Schema(description = "Количество пользователей с email уведомлениями", example = "800")
    private long emailNotificationsEnabled;

    @Schema(description = "Количество пользователей по темам оформления")
    private Map<String, Long> themes;

    @Schema(description = "Количество пользователей по языкам")
    private Map<String, Long> languages;

    @Schema(description = "Количество пользователей по часовым поясам")
    private Map<String, Long> timezones;
}
