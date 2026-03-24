package dn.quest.filestorage.repository;

import dn.quest.filestorage.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Stream;
import java.util.UUID;

/**
 * Репозиторий для работы с метаданными файлов
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID>, JpaSpecificationExecutor<FileMetadata> {

    /**
     * Найти файл по имени хранения
     */
    Optional<FileMetadata> findByStoredFileName(String storedFileName);

    /**
     * Найти файлы по владельцу
     */
    Page<FileMetadata> findByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * Найти все файлы по владельцу
     */
    List<FileMetadata> findAllByOwnerId(UUID ownerId);

    /**
     * Поток всех файлов по владельцу (для избежания проблем с памятью)
     */
    Stream<FileMetadata> streamByOwnerId(UUID ownerId);

    /**
     * Найти файлы по типу файла
     */
    Page<FileMetadata> findByFileType(FileMetadata.FileType fileType, Pageable pageable);

    /**
     * Найти файлы по типу хранилища
     */
    Page<FileMetadata> findByStorageType(FileMetadata.StorageType storageType, Pageable pageable);

    /**
     * Найти временные файлы, которые истекли
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.isTemporary = true AND f.expiresAt < :now")
    List<FileMetadata> findExpiredTemporaryFiles(@Param("now") LocalDateTime now);

    /**
     * Найти публичные файлы
     */
    Page<FileMetadata> findByIsPublicTrue(Pageable pageable);

    /**
     * Найти временные файлы
     */
    Page<FileMetadata> findByIsTemporaryTrue(Pageable pageable);

    /**
     * Найти файлы по владельцу и типу
     */
    Page<FileMetadata> findByOwnerIdAndFileType(UUID ownerId, FileMetadata.FileType fileType, Pageable pageable);

    /**
     * Найти файлы по контент типу
     */
    Page<FileMetadata> findByContentTypeContaining(String contentType, Pageable pageable);

    /**
     * Найти файлы по имени файла (поиск по подстроке)
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.originalFileName LIKE %:fileName%")
    Page<FileMetadata> findByOriginalFileNameContaining(@Param("fileName") String fileName, Pageable pageable);

    /**
     * Увеличить счетчик скачиваний
     */
    @Modifying
    @Query("UPDATE FileMetadata f SET f.downloadCount = f.downloadCount + 1, f.lastAccessedAt = :now WHERE f.id = :fileId")
    void incrementDownloadCount(@Param("fileId") UUID fileId, @Param("now") LocalDateTime now);

    /**
     * Обновить время последнего доступа
     */
    @Modifying
    @Query("UPDATE FileMetadata f SET f.lastAccessedAt = :now WHERE f.id = :fileId")
    void updateLastAccessedAt(@Param("fileId") UUID fileId, @Param("now") LocalDateTime now);

    /**
     * Проверить существование файла по checksum
     */
    boolean existsByChecksum(String checksum);

    /**
     * Найти файл по checksum
     */
    Optional<FileMetadata> findByChecksum(String checksum);

    /**
     * Найти все файлы по questId
     */
    List<FileMetadata> findByQuestId(UUID questId);

    /**
     * Найти все файлы по teamId
     */
    List<FileMetadata> findByTeamId(UUID teamId);

    /**
     * Удалить временные файлы, которые истекли
     */
    @Modifying
    @Query("DELETE FROM FileMetadata f WHERE f.isTemporary = true AND f.expiresAt < :now")
    void deleteExpiredTemporaryFiles(@Param("now") LocalDateTime now);

    /**
     * Удалить все файлы по ID владельца
     */
    @Modifying
    @Query("DELETE FROM FileMetadata f WHERE f.ownerId = :ownerId")
    void deleteAllByOwnerId(@Param("ownerId") UUID ownerId);

    /**
     * Удалить все файлы по questId
     */
    @Modifying
    @Query("DELETE FROM FileMetadata f WHERE f.questId = :questId")
    void deleteAllByQuestId(@Param("questId") UUID questId);

    /**
     * Удалить все файлы по teamId
     */
    @Modifying
    @Query("DELETE FROM FileMetadata f WHERE f.teamId = :teamId")
    void deleteAllByTeamId(@Param("teamId") UUID teamId);

    /**
     * Получить статистику по типам файлов
     */
    @Query("SELECT f.fileType, COUNT(f), SUM(f.fileSize) FROM FileMetadata f GROUP BY f.fileType")
    List<Object[]> getFileStatisticsByType();

    /**
     * Получить статистику по хранилищам
     */
    @Query("SELECT f.storageType, COUNT(f), SUM(f.fileSize) FROM FileMetadata f GROUP BY f.storageType")
    List<Object[]> getFileStatisticsByStorage();

    /**
     * Получить общий размер файлов
     */
    @Query("SELECT SUM(f.fileSize) FROM FileMetadata f")
    Long getTotalStorageSize();

    /**
     * Получить количество файлов
     */
    @Query("SELECT COUNT(f) FROM FileMetadata f")
    Long getTotalFileCount();
}