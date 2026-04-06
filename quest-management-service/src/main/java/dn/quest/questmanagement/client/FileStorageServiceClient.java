package dn.quest.questmanagement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign клиент для интеграции с File Storage Service
 */
@FeignClient(name = "file-storage-service", url = "${file-storage.service.url:http://file-storage-service:8083}")
public interface FileStorageServiceClient {

    /**
     * Загрузить файл
     */
    @PostMapping(value = "/api/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileUploadResponseDTO> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "questId", required = false) UUID questId,
            @RequestParam(value = "levelId", required = false) UUID levelId,
            @RequestParam(value = "mediaType", required = false) String mediaType,
            @RequestParam(value = "isPublic", required = false, defaultValue = "true") Boolean isPublic
    );

    /**
     * Получить файл по ID
     */
    @GetMapping("/api/files/{id}")
    ResponseEntity<Resource> getFile(@PathVariable("id") Long fileId);

    /**
     * Получить информацию о файле по ID
     */
    @GetMapping("/api/files/{id}/info")
    ResponseEntity<FileInfoDTO> getFileInfo(@PathVariable("id") Long fileId);

    /**
     * Удалить файл по ID
     */
    @DeleteMapping("/api/files/{id}")
    ResponseEntity<Void> deleteFile(@PathVariable("id") Long fileId);

    /**
     * Получить файлы квеста
     */
    @GetMapping("/api/files/quest/{questId}")
    ResponseEntity<List<FileInfoDTO>> getQuestFiles(@PathVariable("questId") UUID questId);

    /**
     * Получить файлы уровня
     */
    @GetMapping("/api/files/level/{levelId}")
    ResponseEntity<List<FileInfoDTO>> getLevelFiles(@PathVariable("levelId") UUID levelId);

    /**
     * Получить публичные файлы
     */
    @GetMapping("/api/files/public")
    ResponseEntity<List<FileInfoDTO>> getPublicFiles();

    /**
     * Копировать файлы
     */
    @PostMapping("/api/files/{sourceFileId}/copy")
    ResponseEntity<FileInfoDTO> copyFile(
            @PathVariable("sourceFileId") Long sourceFileId,
            @RequestParam(value = "questId", required = false) UUID questId,
            @RequestParam(value = "levelId", required = false) UUID levelId
    );

    /**
     * Получить статистику хранилища
     */
    @GetMapping("/api/files/statistics")
    ResponseEntity<StorageStatisticsDTO> getStorageStatistics();

    /**
     * DTO для ответа загрузки файла
     */
    class FileUploadResponseDTO {
        private Long id;
        private String fileName;
        private String originalFileName;
        private String filePath;
        private Long fileSize;
        private String mimeType;
        private String mediaType;
        private Boolean isPublic;
        private String downloadUrl;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }

        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    }

    /**
     * DTO для информации о файле
     */
    class FileInfoDTO {
        private Long id;
        private String fileName;
        private String originalFileName;
        private String filePath;
        private Long fileSize;
        private String mimeType;
        private String mediaType;
        private String description;
        private Integer displayOrder;
        private Boolean isPublic;
        private UUID questId;
        private UUID levelId;
        private String createdAt;
        private String updatedAt;
        private String downloadUrl;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

        public UUID getQuestId() { return questId; }
        public void setQuestId(UUID questId) { this.questId = questId; }

        public UUID getLevelId() { return levelId; }
        public void setLevelId(UUID levelId) { this.levelId = levelId; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    }

    /**
     * DTO для статистики хранилища
     */
    class StorageStatisticsDTO {
        private Long totalFiles;
        private Long totalSize;
        private Long publicFiles;
        private Long privateFiles;
        private Map<String, Long> filesByType;
        private Map<String, Long> sizeByType;

        // Getters and setters
        public Long getTotalFiles() { return totalFiles; }
        public void setTotalFiles(Long totalFiles) { this.totalFiles = totalFiles; }

        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }

        public Long getPublicFiles() { return publicFiles; }
        public void setPublicFiles(Long publicFiles) { this.publicFiles = publicFiles; }

        public Long getPrivateFiles() { return privateFiles; }
        public void setPrivateFiles(Long privateFiles) { this.privateFiles = privateFiles; }

        public Map<String, Long> getFilesByType() { return filesByType; }
        public void setFilesByType(Map<String, Long> filesByType) { this.filesByType = filesByType; }

        public Map<String, Long> getSizeByType() { return sizeByType; }
        public void setSizeByType(Map<String, Long> sizeByType) { this.sizeByType = sizeByType; }
    }
}