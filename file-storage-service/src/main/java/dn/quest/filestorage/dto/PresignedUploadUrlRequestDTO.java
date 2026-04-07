package dn.quest.filestorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса предписанного URL для загрузки
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadUrlRequestDTO {

    /**
     * Имя файла
     */
    @NotBlank(message = "Имя файла не может быть пустым")
    private String fileName;

    /**
     * MIME тип файла
     */
    @NotBlank(message = "Тип контента не может быть пустым")
    private String contentType;

    /**
     * Время действия URL в секундах
     */
    @NotNull(message = "Время действия не может быть пустым")
    @Positive(message = "Время действия должно быть положительным")
    private Long durationSeconds;

    /**
     * Путь для хранения файла (опционально)
     */
    private String path;

    /**
     * Тип файла (опционально)
     */
    private dn.quest.filestorage.entity.FileMetadata.FileType fileType;
}