package dn.quest.statistics.repository;

import dn.quest.statistics.entity.FileStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с файловой статистикой
 */
@Repository
public interface FileStatisticsRepository extends JpaRepository<FileStatistics, Long> {

    /**
     * Найти статистику файла по ID и дате
     */
    Optional<FileStatistics> findByFileIdAndDate(String fileId, LocalDate date);

    /**
     * Найти всю статистику файла по ID
     */
    List<FileStatistics> findByFileIdOrderByDateDesc(String fileId);

    /**
     * Найти статистику файла за период
     */
    List<FileStatistics> findByFileIdAndDateBetweenOrderByDateDesc(String fileId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику по владельцу
     */
    List<FileStatistics> findByOwnerIdOrderByDateDesc(Long ownerId);

    /**
     * Найти статистику по владельцу за период
     */
    List<FileStatistics> findByOwnerIdAndDateBetweenOrderByDateDesc(Long ownerId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику по типу сущности
     */
    List<FileStatistics> findByEntityTypeOrderByDateDesc(String entityType);

    /**
     * Найти статистику по типу сущности за период
     */
    List<FileStatistics> findByEntityTypeAndDateBetweenOrderByDateDesc(String entityType, LocalDate startDate, LocalDate endDate);

    /**
     * Получить количество загруженных файлов за дату
     */
    @Query("SELECT COALESCE(SUM(f.uploads), 0) FROM FileStatistics f WHERE f.date = :date")
    Long countUploadedFilesByDate(@Param("date") LocalDate date);

    /**
     * Получить общий размер загруженных файлов за дату (в байтах)
     */
    @Query("SELECT COALESCE(SUM(f.totalSizeBytes), 0) FROM FileStatistics f WHERE f.date = :date")
    Long totalUploadedSizeByDate(@Param("date") LocalDate date);

    /**
     * Получить количество скачиваний файлов за дату
     */
    @Query("SELECT COALESCE(SUM(f.downloads), 0) FROM FileStatistics f WHERE f.date = :date")
    Long countDownloadsByDate(@Param("date") LocalDate date);

    /**
     * Получить количество уникальных скачиваний файлов за дату
     */
    @Query("SELECT COALESCE(SUM(f.uniqueDownloads), 0) FROM FileStatistics f WHERE f.date = :date")
    Long countUniqueDownloadsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по типам файлов за дату
     */
    @Query("SELECT " +
           "f.fileType, " +
           "COUNT(DISTINCT f.fileId) as totalFiles, " +
           "SUM(f.uploads) as uploads, " +
           "SUM(f.totalSizeBytes) as totalSize, " +
           "SUM(f.downloads) as downloads " +
           "FROM FileStatistics f " +
           "WHERE f.date = :date AND f.fileType IS NOT NULL " +
           "GROUP BY f.fileType")
    List<Object[]> getFileTypeStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по MIME типам за дату
     */
    @Query("SELECT " +
           "f.mimeType, " +
           "COUNT(DISTINCT f.fileId) as totalFiles, " +
           "SUM(f.totalSizeBytes) as totalSize " +
           "FROM FileStatistics f " +
           "WHERE f.date = :date AND f.mimeType IS NOT NULL " +
           "GROUP BY f.mimeType " +
           "ORDER BY SUM(f.totalSizeBytes) DESC")
    List<Object[]> getMimeTypeStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по типам сущностей за дату
     */
    @Query("SELECT " +
           "f.entityType, " +
           "COUNT(DISTINCT f.fileId) as totalFiles, " +
           "SUM(f.uploads) as uploads, " +
           "SUM(f.totalSizeBytes) as totalSize " +
           "FROM FileStatistics f " +
           "WHERE f.date = :date AND f.entityType IS NOT NULL " +
           "GROUP BY f.entityType")
    List<Object[]> getEntityTypeStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить топ файлов по количеству скачиваний
     */
    @Query("SELECT f FROM FileStatistics f WHERE f.date = :date ORDER BY f.downloads DESC")
    List<FileStatistics> findTopFilesByDownloads(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ файлов по размеру
     */
    @Query("SELECT f FROM FileStatistics f WHERE f.date = :date ORDER BY f.fileSizeBytes DESC")
    List<FileStatistics> findTopFilesBySize(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить статистику по владельцам файлов за период
     */
    @Query("SELECT " +
           "f.ownerId, " +
           "COUNT(DISTINCT f.fileId) as totalFiles, " +
           "SUM(f.uploads) as uploads, " +
           "SUM(f.totalSizeBytes) as totalSize, " +
           "SUM(f.downloads) as downloads " +
           "FROM FileStatistics f " +
           "WHERE f.date BETWEEN :startDate AND :endDate AND f.ownerId IS NOT NULL " +
           "GROUP BY f.ownerId")
    List<Object[]> getOwnerPeriodStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить средний размер файла за дату
     */
    @Query("SELECT AVG(f.fileSizeBytes) FROM FileStatistics f WHERE f.date = :date AND f.fileSizeBytes IS NOT NULL")
    Double getAvgFileSizeByDate(@Param("date") LocalDate date);

    /**
     * Получить количество публичных файлов за дату
     */
    @Query("SELECT COUNT(DISTINCT f.fileId) FROM FileStatistics f WHERE f.date = :date AND f.isPublic = true")
    Long countPublicFilesByDate(@Param("date") LocalDate date);

    /**
     * Получить количество приватных файлов за дату
     */
    @Query("SELECT COUNT(DISTINCT f.fileId) FROM FileStatistics f WHERE f.date = :date AND f.isPublic = false")
    Long countPrivateFilesByDate(@Param("date") LocalDate date);

    /**
     * Увеличить количество загрузок файла
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.uploads = f.uploads + 1, f.totalSizeBytes = f.totalSizeBytes + :size WHERE f.fileId = :fileId AND f.date = :date")
    int incrementUploads(@Param("fileId") String fileId, @Param("date") LocalDate date, @Param("size") Long size);

    /**
     * Увеличить количество обновлений файла
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.updates = f.updates + 1 WHERE f.fileId = :fileId AND f.date = :date")
    int incrementUpdates(@Param("fileId") String fileId, @Param("date") LocalDate date);

    /**
     * Увеличить количество удалений файла
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.deletions = f.deletions + 1 WHERE f.fileId = :fileId AND f.date = :date")
    int incrementDeletions(@Param("fileId") String fileId, @Param("date") LocalDate date);

    /**
     * Увеличить количество скачиваний файла
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.downloads = f.downloads + 1 WHERE f.fileId = :fileId AND f.date = :date")
    int incrementDownloads(@Param("fileId") String fileId, @Param("date") LocalDate date);

    /**
     * Увеличить количество уникальных скачиваний файла
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.uniqueDownloads = f.uniqueDownloads + 1 WHERE f.fileId = :fileId AND f.date = :date")
    int incrementUniqueDownloads(@Param("fileId") String fileId, @Param("date") LocalDate date);

    /**
     * Увеличить количество просмотров файла
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.views = f.views + 1 WHERE f.fileId = :fileId AND f.date = :date")
    int incrementViews(@Param("fileId") String fileId, @Param("date") LocalDate date);

    /**
     * Увеличить количество уникальных просмотров файла
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.uniqueViews = f.uniqueViews + 1 WHERE f.fileId = :fileId AND f.date = :date")
    int incrementUniqueViews(@Param("fileId") String fileId, @Param("date") LocalDate date);

    /**
     * Обновить время последнего доступа
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.lastAccessTime = :lastAccessTime WHERE f.fileId = :fileId AND f.date = :date")
    int updateLastAccessTime(@Param("fileId") String fileId, @Param("date") LocalDate date, @Param("lastAccessTime") LocalDateTime lastAccessTime);

    /**
     * Увеличить количество сгенерированных предписанных URL
     */
    @Modifying
    @Query("UPDATE FileStatistics f SET f.presignedUrlsGenerated = f.presignedUrlsGenerated + 1, f.totalPresignedUrlHours = f.totalPresignedUrlHours + :hours WHERE f.fileId = :fileId AND f.date = :date")
    int incrementPresignedUrls(@Param("fileId") String fileId, @Param("date") LocalDate date, @Param("hours") Long hours);

    /**
     * Получить статистику по устройствам загрузки
     */
    @Query("SELECT " +
           "f.deviceType, " +
           "COUNT(DISTINCT f.fileId) as totalFiles, " +
           "SUM(f.uploads) as uploads " +
           "FROM FileStatistics f " +
           "WHERE f.date = :date AND f.deviceType IS NOT NULL " +
           "GROUP BY f.deviceType")
    List<Object[]> getDeviceStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по браузерам загрузки
     */
    @Query("SELECT " +
           "f.browser, " +
           "COUNT(DISTINCT f.fileId) as totalFiles, " +
           "SUM(f.uploads) as uploads " +
           "FROM FileStatistics f " +
           "WHERE f.date = :date AND f.browser IS NOT NULL " +
           "GROUP BY f.browser " +
           "ORDER BY SUM(f.uploads) DESC")
    List<Object[]> getBrowserStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить файлы с последним доступом после указанного времени
     */
    List<FileStatistics> findByLastAccessTimeAfter(LocalDateTime time);

    /**
     * Получить статистику по расширениям файлов
     */
    @Query("SELECT " +
           "f.fileExtension, " +
           "COUNT(DISTINCT f.fileId) as totalFiles, " +
           "SUM(f.totalSizeBytes) as totalSize " +
           "FROM FileStatistics f " +
           "WHERE f.date = :date AND f.fileExtension IS NOT NULL " +
           "GROUP BY f.fileExtension " +
           "ORDER BY COUNT(DISTINCT f.fileId) DESC")
    List<Object[]> getFileExtensionStatsByDate(@Param("date") LocalDate date);

    /**
     * Удалить старую статистику (для очистки)
     */
    @Modifying
    @Query("DELETE FROM FileStatistics f WHERE f.date < :date")
    int deleteStatisticsOlderThan(@Param("date") LocalDate date);

    /**
     * Найти статистику за дату
     */
    List<FileStatistics> findByDate(LocalDate date);

    /**
     * Найти статистику до указанной даты
     */
    List<FileStatistics> findByDateBefore(LocalDate date);
}