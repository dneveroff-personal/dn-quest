package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.QuestMedia;
import dn.quest.shared.enums.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с медиа файлами квестов
 */
@Repository
public interface QuestMediaRepository extends JpaRepository<QuestMedia, Long>, JpaSpecificationExecutor<QuestMedia> {

    /**
     * Найти медиа по ID квеста
     */
    List<QuestMedia> findByQuestId(Long questId);

    /**
     * Найти медиа по ID квеста с пагинацией
     */
    Page<QuestMedia> findByQuestId(Long questId, Pageable pageable);

    /**
     * Найти медиа по ID квеста с сортировкой по порядковому номеру
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.questId = :questId ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findByQuestIdOrderByOrderIndex(@Param("questId") Long questId);

    /**
     * Найти медиа по ID уровня
     */
    List<QuestMedia> findByLevelId(Long levelId);

    /**
     * Найти медиа по ID уровня с пагинацией
     */
    Page<QuestMedia> findByLevelId(Long levelId, Pageable pageable);

    /**
     * Найти медиа по ID уровня с сортировкой по порядковому номеру
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.levelId = :levelId ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findByLevelIdOrderByOrderIndex(@Param("levelId") Long levelId);

    /**
     * Найти медиа по типу
     */
    List<QuestMedia> findByMediaType(QuestMedia.MediaType mediaType);

    /**
     * Найти медиа по типу с пагинацией
     */
    Page<QuestMedia> findByMediaType(QuestMedia.MediaType mediaType, Pageable pageable);

    /**
     * Найти медиа по ID квеста и типу
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.questId = :questId AND m.mediaType = :mediaType " +
           "ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findByQuestIdAndMediaType(@Param("questId") Long questId, 
                                               @Param("mediaType") QuestMedia.MediaType mediaType);

    /**
     * Найти медиа по ID уровня и типу
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.levelId = :levelId AND m.mediaType = :mediaType " +
           "ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findByLevelIdAndMediaType(@Param("levelId") Long levelId, 
                                               @Param("mediaType") QuestMedia.MediaType mediaType);

    /**
     * Найти медиа по типу файла
     */
    List<QuestMedia> findByFileType(FileType fileType);

    /**
     * Найти медиа по ID квеста и типу файла
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.questId = :questId AND m.fileType = :fileType " +
           "ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findByQuestIdAndFileType(@Param("questId") Long questId, 
                                              @Param("fileType") FileType fileType);

    /**
     * Найти медиа по ID уровня и типу файла
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.levelId = :levelId AND m.fileType = :fileType " +
           "ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findByLevelIdAndFileType(@Param("levelId") Long levelId, 
                                              @Param("fileType") FileType fileType);

    /**
     * Найти обложки квестов
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.isCover = true AND m.questId IS NOT NULL " +
           "ORDER BY m.questId, m.orderIndex ASC")
    List<QuestMedia> findQuestCovers();

    /**
     * Найти обложку квеста
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.questId = :questId AND m.isCover = true " +
           "ORDER BY m.orderIndex ASC")
    List<QuestMedia> findQuestCover(@Param("questId") Long questId);

    /**
     * Найти активные медиа
     */
    List<QuestMedia> findByActiveTrue();

    /**
     * Найти активные медиа по ID квеста
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.questId = :questId AND m.active = true " +
           "ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findActiveByQuestId(@Param("questId") Long questId);

    /**
     * Найти активные медиа по ID уровня
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.levelId = :levelId AND m.active = true " +
           "ORDER BY m.orderIndex ASC, m.createdAt ASC")
    List<QuestMedia> findActiveByLevelId(@Param("levelId") Long levelId);

    /**
     * Найти медиа по заголовку
     */
    @Query("SELECT m FROM QuestMedia m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY m.title ASC")
    List<QuestMedia> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Найти медиа по описанию
     */
    @Query("SELECT m FROM QuestMedia m WHERE LOWER(m.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY m.description")
    List<QuestMedia> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    /**
     * Найти медиа по оригинальному имени файла
     */
    @Query("SELECT m FROM QuestMedia m WHERE LOWER(m.originalFilename) LIKE LOWER(CONCAT('%', :filename, '%')) " +
           "ORDER BY m.originalFilename ASC")
    List<QuestMedia> findByOriginalFilenameContainingIgnoreCase(@Param("filename") String filename);

    /**
     * Найти медиа по пути к файлу
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.filePath = :filePath")
    Optional<QuestMedia> findByFilePath(@Param("filePath") String filePath);

    /**
     * Найти медиа по URL файла
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.fileUrl = :fileUrl")
    Optional<QuestMedia> findByFileUrl(@Param("fileUrl") String fileUrl);

    /**
     * Найти медиа по MIME типу
     */
    List<QuestMedia> findByMimeType(String mimeType);

    /**
     * Найти медиа по размеру файла
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.fileSizeBytes = :fileSizeBytes " +
           "ORDER BY m.createdAt DESC")
    List<QuestMedia> findByFileSizeBytes(@Param("fileSizeBytes") Long fileSizeBytes);

    /**
     * Найти медиа в диапазоне размеров
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.fileSizeBytes >= :minSize AND m.fileSizeBytes <= :maxSize " +
           "ORDER BY m.fileSizeBytes ASC")
    List<QuestMedia> findByFileSizeBytesBetween(@Param("minSize") Long minSize, 
                                                @Param("maxSize") Long maxSize);

    /**
     * Найти медиа по размерам (изображения)
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.width = :width AND m.height = :height " +
           "ORDER BY m.createdAt DESC")
    List<QuestMedia> findByDimensions(@Param("width") Integer width, @Param("height") Integer height);

    /**
     * Найти медиа по ширине (изображения)
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.width = :width " +
           "ORDER BY m.height ASC, m.createdAt DESC")
    List<QuestMedia> findByWidth(@Param("width") Integer width);

    /**
     * Найти медиа по высоте (изображения)
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.height = :height " +
           "ORDER BY m.width ASC, m.createdAt DESC")
    List<QuestMedia> findByHeight(@Param("height") Integer height);

    /**
     * Найти медиа по длительности (видео/аудио)
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.durationSeconds = :durationSeconds " +
           "ORDER BY m.createdAt DESC")
    List<QuestMedia> findByDurationSeconds(@Param("durationSeconds") Integer durationSeconds);

    /**
     * Найти медиа в диапазоне длительности (видео/аудио)
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.durationSeconds >= :minDuration AND m.durationSeconds <= :maxDuration " +
           "ORDER BY m.durationSeconds ASC")
    List<QuestMedia> findByDurationSecondsBetween(@Param("minDuration") Integer minDuration, 
                                                  @Param("maxDuration") Integer maxDuration);

    /**
     * Найти медиа созданные пользователем
     */
    List<QuestMedia> findByCreatedBy(Long createdBy);

    /**
     * Найти медиа созданные пользователем с пагинацией
     */
    Page<QuestMedia> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * Подсчет медиа по ID квеста
     */
    @Query("SELECT COUNT(m) FROM QuestMedia m WHERE m.questId = :questId")
    long countByQuestId(@Param("questId") Long questId);

    /**
     * Подсчет медиа по ID уровня
     */
    @Query("SELECT COUNT(m) FROM QuestMedia m WHERE m.levelId = :levelId")
    long countByLevelId(@Param("levelId") Long levelId);

    /**
     * Подсчет медиа по типу
     */
    long countByMediaType(QuestMedia.MediaType mediaType);

    /**
     * Подсчет медиа по типу файла
     */
    long countByFileType(FileType fileType);

    /**
     * Подсчет активных медиа по ID квеста
     */
    @Query("SELECT COUNT(m) FROM QuestMedia m WHERE m.questId = :questId AND m.active = true")
    long countActiveByQuestId(@Param("questId") Long questId);

    /**
     * Подсчет активных медиа по ID уровня
     */
    @Query("SELECT COUNT(m) FROM QuestMedia m WHERE m.levelId = :levelId AND m.active = true")
    long countActiveByLevelId(@Param("levelId") Long levelId);

    /**
     * Проверить существование медиа по пути к файлу
     */
    boolean existsByFilePath(String filePath);

    /**
     * Проверить существование медиа по URL файла
     */
    boolean existsByFileUrl(String fileUrl);

    /**
     * Найти максимальный порядковый номер медиа для квеста
     */
    @Query("SELECT COALESCE(MAX(m.orderIndex), 0) FROM QuestMedia m WHERE m.questId = :questId")
    Integer findMaxOrderIndexByQuestId(@Param("questId") Long questId);

    /**
     * Найти максимальный порядковый номер медиа для уровня
     */
    @Query("SELECT COALESCE(MAX(m.orderIndex), 0) FROM QuestMedia m WHERE m.levelId = :levelId")
    Integer findMaxOrderIndexByLevelId(@Param("levelId") Long levelId);

    /**
     * Обновить порядковый номер медиа
     */
    @Modifying
    @Query("UPDATE QuestMedia m SET m.orderIndex = :newOrderIndex, m.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE m.id = :mediaId")
    int updateOrderIndex(@Param("mediaId") Long mediaId, @Param("newOrderIndex") Integer newOrderIndex);

    /**
     * Активировать медиа
     */
    @Modifying
    @Query("UPDATE QuestMedia m SET m.active = true, m.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE m.id IN :mediaIds")
    int activateMedia(@Param("mediaIds") List<Long> mediaIds);

    /**
     * Деактивировать медиа
     */
    @Modifying
    @Query("UPDATE QuestMedia m SET m.active = false, m.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE m.id IN :mediaIds")
    int deactivateMedia(@Param("mediaIds") List<Long> mediaIds);

    /**
     * Установить обложку квеста
     */
    @Modifying
    @Query("UPDATE QuestMedia m SET m.isCover = :isCover, m.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE m.id = :mediaId")
    int setCover(@Param("mediaId") Long mediaId, @Param("isCover") Boolean isCover);

    /**
     * Удалить обложки квеста
     */
    @Modifying
    @Query("UPDATE QuestMedia m SET m.isCover = false, m.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE m.questId = :questId")
    int removeQuestCovers(@Param("questId") Long questId);

    /**
     * Удалить медиа по ID квеста
     */
    @Modifying
    @Query("DELETE FROM QuestMedia m WHERE m.questId = :questId")
    int deleteByQuestId(@Param("questId") Long questId);

    /**
     * Удалить медиа по ID уровня
     */
    @Modifying
    @Query("DELETE FROM QuestMedia m WHERE m.levelId = :levelId")
    int deleteByLevelId(@Param("levelId") Long levelId);

    /**
     * Получить статистику по медиа квеста
     */
    @Query("SELECT " +
           "COUNT(m) as totalMedia, " +
           "COUNT(CASE WHEN m.active = true THEN 1 END) as activeMedia, " +
           "COUNT(CASE WHEN m.mediaType = 'IMAGE' THEN 1 END) as images, " +
           "COUNT(CASE WHEN m.mediaType = 'VIDEO' THEN 1 END) as videos, " +
           "COUNT(CASE WHEN m.mediaType = 'AUDIO' THEN 1 END) as audio, " +
           "COUNT(CASE WHEN m.mediaType = 'DOCUMENT' THEN 1 END) as documents, " +
           "COUNT(CASE WHEN m.isCover = true THEN 1 END) as covers, " +
           "COALESCE(SUM(m.fileSizeBytes), 0) as totalSize " +
           "FROM QuestMedia m WHERE m.questId = :questId")
    Object[] getQuestMediaStatistics(@Param("questId") Long questId);

    /**
     * Получить статистику по медиа уровня
     */
    @Query("SELECT " +
           "COUNT(m) as totalMedia, " +
           "COUNT(CASE WHEN m.active = true THEN 1 END) as activeMedia, " +
           "COUNT(CASE WHEN m.mediaType = 'IMAGE' THEN 1 END) as images, " +
           "COUNT(CASE WHEN m.mediaType = 'VIDEO' THEN 1 END) as videos, " +
           "COUNT(CASE WHEN m.mediaType = 'AUDIO' THEN 1 END) as audio, " +
           "COUNT(CASE WHEN m.mediaType = 'DOCUMENT' THEN 1 END) as documents, " +
           "COALESCE(SUM(m.fileSizeBytes), 0) as totalSize " +
           "FROM QuestMedia m WHERE m.levelId = :levelId")
    Object[] getLevelMediaStatistics(@Param("levelId") Long levelId);

    /**
     * Найти самые большие файлы
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.fileSizeBytes IS NOT NULL " +
           "ORDER BY m.fileSizeBytes DESC")
    List<QuestMedia> findLargestFiles(Pageable pageable);

    /**
     * Найти медиа для копирования (активные медиа активных квестов/уровней)
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.active = true AND " +
           "(m.questId IN (SELECT q.id FROM Quest q WHERE q.published = true AND q.archived = false) " +
           "OR m.levelId IN (SELECT l.id FROM Level l WHERE l.active = true)) " +
           "ORDER BY m.questId, m.levelId, m.orderIndex ASC")
    List<QuestMedia> findMediaForCopying();

    /**
     * Найти неиспользуемые медиа файлы
     */
    @Query("SELECT m FROM QuestMedia m WHERE m.questId IS NULL AND m.levelId IS NULL " +
           "ORDER BY m.createdAt DESC")
    List<QuestMedia> findOrphanedMedia();

    /**
     * Получить общий размер всех медиа файлов
     */
    @Query("SELECT COALESCE(SUM(m.fileSizeBytes), 0) FROM QuestMedia m WHERE m.active = true")
    Long getTotalActiveMediaSize();

    /**
     * Получить общий размер медиа по типу
     */
    @Query("SELECT COALESCE(SUM(m.fileSizeBytes), 0) FROM QuestMedia m WHERE m.mediaType = :mediaType AND m.active = true")
    Long getTotalSizeByMediaType(@Param("mediaType") QuestMedia.MediaType mediaType);
}