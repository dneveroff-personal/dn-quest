package dn.quest.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа на пакетную загрузку файлов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchFileUploadResponseDTO {

    /**
     * Общее количество файлов в запросе
     */
    private Integer totalFiles;

    /**
     * Количество успешно загруженных файлов
     */
    private Integer successfulUploads;

    /**
     * Количество файлов с ошибками
     */
    private Integer failedUploads;

    /**
     * Список успешно загруженных файлов
     */
    private List<FileResponseDTO> uploadedFiles;

    /**
     * Список ошибок загрузки
     */
    private List<FileUploadErrorDTO> errors;

    /**
     * Общий размер загруженных файлов в байтах
     */
    private Long totalUploadedSize;

    /**
     * Время выполнения операции в миллисекундах
     */
    private Long processingTimeMs;

    /**
     * DTO для информации об ошибке загрузки файла
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadErrorDTO {
        
        /**
         * Имя файла
         */
        private String fileName;
        
        /**
         * Код ошибки
         */
        private String errorCode;
        
        /**
         * Сообщение об ошибке
         */
        private String errorMessage;
        
        /**
         * Детали ошибки
         */
        private String errorDetails;
    }
}