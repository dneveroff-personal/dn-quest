package dn.quest.gameengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для отображения данных о завершении уровня в лидерборде
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelCompletionDTO {

    /**
     * ID уровня
     */
    private UUID levelId;

    /**
     * Название уровня
     */
    private String levelTitle;

    /**
     * ID команды
     */
    private UUID teamId;

    /**
     * Название команды
     */
    private String teamName;

    /**
     * ID пользователя
     */
    private UUID userId;

    /**
     * Отображаемое имя пользователя
     */
    private String userDisplayName;

    /**
     * Время прохождения
     */
    private Instant passTime;

    /**
     * Длительность в формате HH:mm:ss
     */
    private String duration;

    /**
     * Бонусное время на уровне в секундах
     */
    private Integer bonusOnLevelSec;

    /**
     * Штрафное время на уровне в секундах
     */
    private Integer penaltyOnLevelSec;
}