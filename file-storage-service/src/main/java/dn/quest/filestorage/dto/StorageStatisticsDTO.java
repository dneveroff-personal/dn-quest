package dn.quest.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для статистики хранилища
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageStatisticsDTO {

    /**
     * Общее количество файлов
     */
    private Long totalFiles;

    /**
     * Общий размер хранилища в байтах
     */
    private Long totalSizeBytes;

    /**
     * Общий размер в человекочитаемом формате
     */
    private String formattedTotalSize;

    /**
     * Доступное место в байтах
     */
    private Long availableSpaceBytes;

    /**
     * Доступное место в человекочитаемом формате
     */
    private String formattedAvailableSpace;

    /**
     * Использованное место в процентах
     */
    private Double usagePercentage;

    /**
     * Количество временных файлов
     */
    private Long temporaryFiles;

    /**
     * Размер временных файлов в байтах
     */
    private Long temporaryFilesSizeBytes;

    /**
     * Размер временных файлов в человекочитаемом формате
     */
    private String formattedTemporaryFilesSize;

    /**
     * Статистика по типам файлов
     */
    private Map<String, FileTypeStatistics> fileTypeStatistics;

    /**
     * Статистика по хранилищам
     */
    private Map<String, StorageTypeStatistics> storageTypeStatistics;

    /**
     * Время последней очистки
     */
    private java.time.LocalDateTime lastCleanupTime;

    /**
     * Статус доступности хранилища
     */
    private Boolean isAvailable;

    /**
     * Среднее время доступа в миллисекундах
     */
    private Double averageAccessTimeMs;

    /**
     * Количество ошибок за последний час
     */
    private Long errorCountLastHour;

    /**
     * DTO для статистики по типу файла
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileTypeStatistics {
        
        /**
         * Тип файла
         */
        private String fileType;
        
        /**
         * Количество файлов
         */
        private Long count;
        
        /**
         * Общий размер в байтах
         */
        private Long sizeBytes;
        
        /**
         * Общий размер в человекочитаемом формате
         */
        private String formattedSize;
        
        /**
         * Средний размер файла
         */
        private Double averageSizeBytes;
        
        /**
         * Средний размер в человекочитаемом формате
         */
        private String formattedAverageSize;
    }

    /**
     * DTO для статистики по типу хранилища
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorageTypeStatistics {
        
        /**
         * Тип хранилища
         */
        private String storageType;
        
        /**
         * Количество файлов
         */
        private Long count;
        
        /**
         * Общий размер в байтах
         */
        private Long sizeBytes;
        
        /**
         * Общий размер в человекочитаемом формате
         */
        private String formattedSize;
        
        /**
         * Доступно ли хранилище
         */
        private Boolean isAvailable;
        
        /**
         * Использование в процентах
         */
        private Double usagePercentage;
    }

    /**
     * Получить отформатированный размер
     */
    public static String formatSize(Long sizeBytes) {
        if (sizeBytes == null || sizeBytes == 0) {
            return "0 B";
        }
        return FileResponseDTO.formatFileSize(sizeBytes);
    }
}