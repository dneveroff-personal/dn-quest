package dn.quest.notification.repository;

import dn.quest.notification.entity.NotificationTemplate;
import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с шаблонами уведомлений
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Найти шаблон по ID
     */
    Optional<NotificationTemplate> findByTemplateId(String templateId);

    /**
     * Найти активные шаблоны по типу и категории
     */
    Optional<NotificationTemplate> findByTypeAndCategoryAndLanguageAndActiveTrueOrderByVersionDesc(
            NotificationType type, 
            NotificationCategory category, 
            String language);

    /**
     * Найти все шаблоны по типу и категории
     */
    List<NotificationTemplate> findByTypeAndCategoryOrderByVersionDesc(
            NotificationType type, 
            NotificationCategory category);

    /**
     * Найти все шаблоны по типу
     */
    List<NotificationTemplate> findByTypeOrderByVersionDesc(NotificationType type);

    /**
     * Найти все шаблоны по категории
     */
    List<NotificationTemplate> findByCategoryOrderByVersionDesc(NotificationCategory category);

    /**
     * Найти все активные шаблоны
     */
    List<NotificationTemplate> findByActiveTrueOrderByVersionDesc();

    /**
     * Найти шаблоны по языку
     */
    List<NotificationTemplate> findByLanguageOrderByVersionDesc(String language);

    /**
     * Найти шаблоны по автору
     */
    List<NotificationTemplate> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * Проверить существование шаблона по ID
     */
    boolean existsByTemplateId(String templateId);

    /**
     * Найти шаблоны для поиска
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:language IS NULL OR t.language = :language) AND " +
           "(:active IS NULL OR t.active = :active) AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<NotificationTemplate> searchTemplates(
            @Param("type") NotificationType type,
            @Param("category") NotificationCategory category,
            @Param("language") String language,
            @Param("active") Boolean active,
            @Param("search") String search);

    /**
     * Найти последнюю версию шаблона по типу, категории и языку
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE " +
           "t.type = :type AND t.category = :category AND t.language = :language " +
           "ORDER BY t.version DESC")
    List<NotificationTemplate> findLatestVersion(
            @Param("type") NotificationType type,
            @Param("category") NotificationCategory category,
            @Param("language") String language);

    /**
     * Подсчитать количество шаблонов по типу
     */
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.type = :type")
    long countByType(@Param("type") NotificationType type);

    /**
     * Подсчитать количество шаблонов по категории
     */
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.category = :category")
    long countByCategory(@Param("category") NotificationCategory category);

    /**
     * Подсчитать количество активных шаблонов
     */
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.active = true")
    long countActiveTemplates();
}