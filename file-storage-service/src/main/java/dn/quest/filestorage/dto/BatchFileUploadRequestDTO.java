package dn.quest.filestorage.dto;

import dn.quest.filestorage.entity.FileMetadata;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO для запроса пакетной загрузки файлов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchFileUploadRequestDTO {

    /**
     * Тип файла для всех файлов в пакете
     */
    @NotNull(message = "Тип файла не может быть пустым")
    private FileMetadata.FileType fileType;

    /**
     * Тип хранилища (опционально, будет выбран автоматически если не указан)
     */
    private FileMetadata.StorageType storageType;

    /**
     * Общее описание для всех файлов
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    /**
     * Базовый путь для всех файлов
     */
    @Size(max = 500, message = "Путь не должен превышать 500 символов")
    private String basePath;

    /**
     * Являются ли файлы публичными
     */
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Являются ли файлы временными
     */
    @Builder.Default
    private Boolean isTemporary = false;

    /**
     * Время истечения для временных файлов
     */
    private java.time.LocalDateTime expiresAt;

    /**
     * Общие метаданные для всех файлов
     */
    private Map<String, String> commonMetadata;

    /**
     * Индивидуальные метаданные для каждого файла
     */
    private Map<String, Map<String, String>> individualMetadata;
}