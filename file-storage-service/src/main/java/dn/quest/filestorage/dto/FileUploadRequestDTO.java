package dn.quest.filestorage.dto;

import dn.quest.filestorage.entity.FileMetadata;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса загрузки файла
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequestDTO {

    /**
     * Тип файла
     */
    @NotNull(message = "Тип файла не может быть пустым")
    private FileMetadata.FileType fileType;

    /**
     * Тип хранилища (опционально, будет выбран автоматически если не указан)
     */
    private FileMetadata.StorageType storageType;

    /**
     * Описание файла
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    /**
     * Путь в хранилище (опционально)
     */
    @Size(max = 500, message = "Путь не должен превышать 500 символов")
    private String path;

    /**
     * Является ли файл публичным
     */
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Является ли файл временным
     */
    @Builder.Default
    private Boolean isTemporary = false;

    /**
     * Время истечения для временных файлов
     */
    private java.time.LocalDateTime expiresAt;

    /**
     * Дополнительные метаданные
     */
    private java.util.Map<String, String> metadata;
}