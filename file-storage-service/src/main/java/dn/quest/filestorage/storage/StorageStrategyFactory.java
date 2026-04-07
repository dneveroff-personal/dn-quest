package dn.quest.filestorage.storage;

import dn.quest.filestorage.entity.FileMetadata;
import dn.quest.filestorage.storage.impl.LocalStorageStrategy;
import dn.quest.filestorage.storage.impl.MinioStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Фабрика для создания стратегий хранения файлов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StorageStrategyFactory {

    private final LocalStorageStrategy localStorageStrategy;
    private final MinioStorageStrategy minioStorageStrategy;

    @Value("${file.storage.default-type:LOCAL}")
    private String defaultStorageType;

    /**
     * Получить стратегию хранения по типу
     *
     * @param storageType тип хранилища
     * @return стратегия хранения
     */
    public StorageStrategy getStrategy(FileMetadata.StorageType storageType) {
        switch (storageType) {
            case LOCAL:
                return localStorageStrategy;
            case MINIO:
                return minioStorageStrategy;
            case S3:
                // TODO: Реализовать S3 стратегию
                log.warn("S3 хранилище еще не реализовано, используется LOCAL");
                return localStorageStrategy;
            case CDN:
                // TODO: Реализовать CDN стратегию
                log.warn("CDN хранилище еще не реализовано, используется LOCAL");
                return localStorageStrategy;
            default:
                log.warn("Неизвестный тип хранилища: {}, используется стратегия по умолчанию", storageType);
                return getDefaultStrategy();
        }
    }

    /**
     * Получить стратегию хранения по строковому типу
     *
     * @param storageType тип хранилища в виде строки
     * @return стратегия хранения
     */
    public StorageStrategy getStrategy(String storageType) {
        try {
            FileMetadata.StorageType type = FileMetadata.StorageType.valueOf(storageType.toUpperCase());
            return getStrategy(type);
        } catch (IllegalArgumentException e) {
            log.warn("Неизвестный тип хранилища: {}, используется стратегия по умолчанию", storageType);
            return getDefaultStrategy();
        }
    }

    /**
     * Получить стратегию хранения по умолчанию
     *
     * @return стратегия хранения по умолчанию
     */
    public StorageStrategy getDefaultStrategy() {
        return getStrategy(defaultStorageType);
    }

    /**
     * Получить оптимальную стратегию для типа файла
     *
     * @param fileType тип файла
     * @return оптимальная стратегия хранения
     */
    public StorageStrategy getOptimalStrategy(FileMetadata.FileType fileType) {
        switch (fileType) {
            case AVATAR:
                // Аватары лучше хранить в MinIO для быстрого доступа
                return minioStorageStrategy;
            case QUEST_MEDIA:
                // Медиа файлы квестов в MinIO
                return minioStorageStrategy;
            case TEMPORARY:
                // Временные файлы в локальном хранилище
                return localStorageStrategy;
            case LEVEL_FILE:
                // Файлы уровней в MinIO
                return minioStorageStrategy;
            default:
                return getDefaultStrategy();
        }
    }

    /**
     * Получить оптимальный тип хранилища для типа файла
     *
     * @param fileType тип файла
     * @return оптимальный тип хранилища
     */
    public FileMetadata.StorageType getOptimalStorageType(FileMetadata.FileType fileType) {
        switch (fileType) {
            case AVATAR:
                return FileMetadata.StorageType.MINIO;
            case QUEST_MEDIA:
                return FileMetadata.StorageType.MINIO;
            case TEMPORARY:
                return FileMetadata.StorageType.LOCAL;
            case LEVEL_FILE:
                return FileMetadata.StorageType.MINIO;
            default:
                return FileMetadata.StorageType.valueOf(defaultStorageType.toUpperCase());
        }
    }

    /**
     * Получить стратегию на основе размера файла
     *
     * @param fileSize размер файла в байтах
     * @return оптимальная стратегия хранения
     */
    public StorageStrategy getStrategyByFileSize(long fileSize) {
        // Файлы больше 10MB лучше хранить в MinIO
        if (fileSize > 10 * 1024 * 1024) {
            return minioStorageStrategy;
        }
        return getDefaultStrategy();
    }

    /**
     * Проверить доступность стратегии
     *
     * @param storageType тип хранилища
     * @return true если стратегия доступна
     */
    public boolean isStrategyAvailable(FileMetadata.StorageType storageType) {
        try {
            StorageStrategy strategy = getStrategy(storageType);
            return strategy.isStorageAvailable();
        } catch (Exception e) {
            log.error("Ошибка при проверке доступности стратегии: {}", storageType, e);
            return false;
        }
    }

    /**
     * Получить резервную стратегию
     *
     * @param primaryStrategy основная стратегия
     * @return резервная стратегия
     */
    public StorageStrategy getFallbackStrategy(StorageStrategy primaryStrategy) {
        if (primaryStrategy instanceof LocalStorageStrategy) {
            return minioStorageStrategy;
        } else if (primaryStrategy instanceof MinioStorageStrategy) {
            return localStorageStrategy;
        }
        return localStorageStrategy;
    }

    /**
     * Инициализировать все стратегии
     */
    public void initializeAllStrategies() {
        log.info("Инициализация стратегий хранения файлов");
        
        try {
            localStorageStrategy.initializeStorage();
            log.info("Локальное хранилище инициализировано");
        } catch (Exception e) {
            log.error("Ошибка при инициализации локального хранилища", e);
        }

        try {
            minioStorageStrategy.initializeStorage();
            log.info("MinIO хранилище инициализировано");
        } catch (Exception e) {
            log.error("Ошибка при инициализации MinIO хранилища", e);
        }
    }

    /**
     * Получить статистику по всем хранилищам
     *
     * @return карта статистики по типам хранилищ
     */
    public java.util.Map<String, StorageStatistics> getAllStorageStatistics() {
        java.util.Map<String, StorageStatistics> statistics = new java.util.HashMap<>();

        try {
            StorageStatistics localStats = localStorageStrategy.getStorageStatistics();
            statistics.put("LOCAL", localStats);
        } catch (Exception e) {
            log.error("Ошибка при получении статистики локального хранилища", e);
        }

        try {
            StorageStatistics minioStats = minioStorageStrategy.getStorageStatistics();
            statistics.put("MINIO", minioStats);
        } catch (Exception e) {
            log.error("Ошибка при получении статистики MinIO хранилища", e);
        }

        return statistics;
    }

    /**
     * Очистить временные файлы во всех хранилищах
     */
    public void cleanupAllTempFiles() {
        log.info("Очистка временных файлов во всех хранилищах");

        try {
            localStorageStrategy.cleanupTempFiles();
            log.info("Временные файлы в локальном хранилище очищены");
        } catch (Exception e) {
            log.error("Ошибка при очистке временных файлов в локальном хранилище", e);
        }

        try {
            minioStorageStrategy.cleanupTempFiles();
            log.info("Временные файлы в MinIO хранилище очищены");
        } catch (Exception e) {
            log.error("Ошибка при очистке временных файлов в MinIO хранилище", e);
        }
    }
}