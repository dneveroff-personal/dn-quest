package dn.quest.teammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для статистики пользователей
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    
    /**
     * Общее количество пользователей
     */
    private Long totalUsers;
    
    /**
     * Количество активных пользователей
     */
    private Long activeUsers;
    
    /**
     * Количество неактивных пользователей
     */
    private Long inactiveUsers;
    
    /**
     * Количество пользователей, состоящих в командах
     */
    private Long usersInTeams;
    
    /**
     * Количество капитанов команд
     */
    private Long teamCaptains;
    
    /**
     * Среднее количество команд на пользователя
     */
    private Double averageTeamsPerUser;
    
    /**
     * Количество новых пользователей за последний месяц
     */
    private Long newUsersThisMonth;
    
    /**
     * Количество пользователей, присоединившихся к командам за последний месяц
     */
    private Long usersJoinedTeamsThisMonth;
    
    /**
     * Самый активный пользователь
     */
    private UserDTO mostActiveUser;
    
    /**
     * Пользователь с наибольшим количеством команд
     */
    private UserDTO userWithMostTeams;
}