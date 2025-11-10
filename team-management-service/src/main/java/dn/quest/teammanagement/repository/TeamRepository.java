package dn.quest.teammanagement.repository;

import dn.quest.teammanagement.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с командами
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Найти команду по названию
     */
    Optional<Team> findByName(String name);

    /**
     * Найти команды по капитану
     */
    List<Team> findByCaptainId(Long captainId);

    /**
     * Найти активные команды
     */
    List<Team> findByIsActiveTrue();

    /**
     * Найти публичные команды
     */
    List<Team> findByIsPrivateFalseAndIsActiveTrue();

    /**
     * Найти команды по названию (с учетом регистра)
     */
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) AND t.isActive = true")
    Page<Team> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name, Pageable pageable);

    /**
     * Найти команды по названию и приватности
     */
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) AND t.isPrivate = :isPrivate AND t.isActive = true")
    Page<Team> findByNameContainingIgnoreCaseAndIsPrivateAndIsActiveTrue(
            @Param("name") String name, 
            @Param("isPrivate") Boolean isPrivate, 
            Pageable pageable);

    /**
     * Найти команды по тегам
     */
    @Query("SELECT DISTINCT t FROM Team t " +
           "JOIN t.settings s " +
           "WHERE s.teamTags IS NOT NULL " +
           "AND LOWER(s.teamTags) LIKE LOWER(CONCAT('%', :tag, '%')) " +
           "AND t.isPrivate = false " +
           "AND t.isActive = true")
    Page<Team> findByTeamTagsContainingAndIsPublicAndActive(@Param("tag") String tag, Pageable pageable);

    /**
     * Найти команды, в которых состоит пользователь
     */
    @Query("SELECT DISTINCT tm.team FROM TeamMember tm WHERE tm.user.id = :userId AND tm.isActive = true")
    List<Team> findTeamsByUserId(@Param("userId") Long userId);

    /**
     * Найти активные команды, в которых состоит пользователь
     */
    @Query("SELECT DISTINCT tm.team FROM TeamMember tm " +
           "JOIN tm.team t " +
           "WHERE tm.user.id = :userId " +
           "AND tm.isActive = true " +
           "AND t.isActive = true")
    List<Team> findActiveTeamsByUserId(@Param("userId") Long userId);

    /**
     * Проверить, существует ли команда с таким названием
     */
    boolean existsByName(String name);

    /**
     * Проверить, является ли пользователь капитаном команды
     */
    boolean existsByIdAndCaptainId(Long teamId, Long captainId);

    /**
     * Найти команды с количеством участников меньше максимального
     */
    @Query("SELECT t FROM Team t WHERE t.isActive = true AND (t.maxMembers IS NULL OR " +
           "(SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = t.id AND tm.isActive = true) < t.maxMembers)")
    Page<Team> findTeamsWithAvailableSlots(Pageable pageable);

    /**
     * Найти команды по рейтингу (топ N)
     */
    @Query("SELECT t FROM Team t " +
           "JOIN t.statistics s " +
           "WHERE t.isActive = true " +
           "ORDER BY s.rating DESC")
    Page<Team> findTopTeamsByRating(Pageable pageable);

    /**
     * Найти команды по количеству участников
     */
    @Query("SELECT t FROM Team t " +
           "WHERE t.isActive = true " +
           "ORDER BY (SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = t.id AND tm.isActive = true) DESC")
    Page<Team> findTeamsByMemberCount(Pageable pageable);

    /**
     * Найти команды, созданные за определенный период
     */
    @Query("SELECT t FROM Team t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.isActive = true")
    List<Team> findTeamsByCreationPeriod(@Param("startDate") java.time.Instant startDate, 
                                        @Param("endDate") java.time.Instant endDate);

    /**
     * Получить статистику по командам
     */
    @Query("SELECT COUNT(t) FROM Team t WHERE t.isActive = true")
    long countActiveTeams();

    /**
     * Получить статистику по приватным командам
     */
    @Query("SELECT COUNT(t) FROM Team t WHERE t.isPrivate = true AND t.isActive = true")
    long countPrivateTeams();

    /**
     * Получить статистику по публичным командам
     */
    @Query("SELECT COUNT(t) FROM Team t WHERE t.isPrivate = false AND t.isActive = true")
    long countPublicTeams();
}