package dn.quest.filestorage.dto;

import dn.quest.filestorage.entity.FileMetadata;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для запроса поиска файлов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchRequestDTO {

    /**
     * ID владельца файла
     */
    private java.util.UUID ownerId;

    /**
     * Тип файла
     */
    private FileMetadata.FileType fileType;

    /**
     * Тип хранилища
     */
    private FileMetadata.StorageType storageType;

    /**
     * MIME тип файла (частичное совпадение)
     */
    private String contentType;

    /**
     * Имя файла (частичное совпадение)
     */
    private String fileName;

    /**
     * Является ли файл публичным
     */
    private Boolean isPublic;

    /**
     * Является ли файл временным
     */
    private Boolean isTemporary;

    /**
     * Минимальный размер файла
     */
    @Min(value = 0, message = "Минимальный размер файла не может быть отрицательным")
    private Long minFileSize;

    /**
     * Максимальный размер файла
     */
    @Min(value = 0, message = "Максимальный размер файла не может быть отрицательным")
    private Long maxFileSize;

    /**
     * Дата создания с
     */
    private LocalDateTime createdFrom;

    /**
     * Дата создания по
     */
    private LocalDateTime createdTo;

    /**
     * Дата последнего доступа с
     */
    private LocalDateTime lastAccessedFrom;

    /**
     * Дата последнего доступа по
     */
    private LocalDateTime lastAccessedTo;

    /**
     * Список ID файлов
     */
    private List<java.util.UUID> fileIds;

    /**
     * Номер страницы (начиная с 0)
     */
    @Builder.Default
    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private int page = 0;

    /**
     * Размер страницы
     */
    @Builder.Default
    @Min(value = 1, message = "Размер страницы должен быть больше 0")
    private int size = 20;

    /**
     * Поле сортировки
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Направление сортировки
     */
    @Builder.Default
    private String sortDirection = "desc";

    /**
     * Получить направление сортировки в формате Spring Data
     */
    public org.springframework.data.domain.Sort.Direction getSortDirection() {
        return "asc".equalsIgnoreCase(sortDirection) 
                ? org.springframework.data.domain.Sort.Direction.ASC 
                : org.springframework.data.domain.Sort.Direction.DESC;
    }
}