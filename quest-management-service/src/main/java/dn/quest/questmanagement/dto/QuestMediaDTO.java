package dn.quest.questmanagement.dto;

import dn.quest.questmanagement.entity.QuestMedia;
import dn.quest.shared.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для представления медиа файла квеста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestMediaDTO extends BaseDTO {

    /**
     * ID квеста (null если медиа относится к уровню)
     */
    private Long questId;

    /**
     * ID уровня (null если медиа относится к квесту)
     */
    private Long levelId;

    /**
     * Тип медиа
     */
    @NotNull(message = "Тип медиа обязателен")
    private QuestMedia.MediaType mediaType;

    /**
     * Тип файла
     */
    @NotNull(message = "Тип файла обязателен")
    private QuestMedia.FileType fileType;

    /**
     * Оригинальное имя файла
     */
    @NotBlank(message = "Имя файла не может быть пустым")
    @Size(max = 255, message = "Имя файла не должно превышать 255 символов")
    private String originalFilename;

    /**
     * Путь к файлу в хранилище
     */
    @NotBlank(message = "Путь к файлу не может быть пустым")
    @Size(max = 1000, message = "Путь к файлу не должен превышать 1000 символов")
    private String filePath;

    /**
     * URL файла для доступа
     */
    @Size(max = 1000, message = "URL файла не должен превышать 1000 символов")
    private String fileUrl;

    /**
     * Размер файла в байтах
     */
    private Long fileSizeBytes;

    /**
     * MIME тип файла
     */
    @Size(max = 100, message = "MIME тип не должен превышать 100 символов")
    private String mimeType;

    /**
     * Заголовок медиа
     */
    @Size(max = 200, message = "Заголовок не должен превышать 200 символов")
    private String title;

    /**
     * Описание медиа
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    /**
     * Порядковый номер медиа
     */
    private Integer orderIndex;

    /**
     * Активно ли медиа
     */
    private Boolean active;

    /**
     * Является ли медиа обложкой
     */
    private Boolean isCover;

    /**
     * Ширина изображения (для изображений)
     */
    private Integer width;

    /**
     * Высота изображения (для изображений)
     */
    private Integer height;

    /**
     * Длительность видео/аудио в секундах
     */
    private Integer durationSeconds;

    /**
     * Дата создания
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * ID пользователя, загрузившего медиа
     */
    private Long createdBy;

    /**
     * ID пользователя, обновившего медиа
     */
    private Long updatedBy;

    /**
     * Версия медиа
     */
    private Long version;

    /**
     * Проверяет, относится ли медиа к квесту
     */
    public boolean isQuestMedia() {
        return questId != null && levelId == null;
    }

    /**
     * Проверяет, относится ли медиа к уровню
     */
    public boolean isLevelMedia() {
        return levelId != null;
    }

    /**
     * Проверяет, является ли медиа изображением
     */
    public boolean isImage() {
        return mediaType == QuestMedia.MediaType.IMAGE;
    }

    /**
     * Проверяет, является ли медиа видео
     */
    public boolean isVideo() {
        return mediaType == QuestMedia.MediaType.VIDEO;
    }

    /**
     * Проверяет, является ли медиа аудио
     */
    public boolean isAudio() {
        return mediaType == QuestMedia.MediaType.AUDIO;
    }

    /**
     * Проверяет, является ли медиа документом
     */
    public boolean isDocument() {
        return mediaType == QuestMedia.MediaType.DOCUMENT;
    }

    /**
     * Проверяет, является ли медиа архивом
     */
    public boolean isArchive() {
        return mediaType == QuestMedia.MediaType.ARCHIVE;
    }

    /**
     * Проверяет, активно ли медиа
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Проверяет, является ли медиа обложкой
     */
    public boolean isCover() {
        return isCover != null && isCover;
    }

    /**
     * Проверяет, есть ли у медиа размеры
     */
    public boolean hasDimensions() {
        return width != null && height != null;
    }

    /**
     * Проверяет, есть ли у медиа длительность
     */
    public boolean hasDuration() {
        return durationSeconds != null && durationSeconds > 0;
    }

    /**
     * Проверяет, есть ли у медиа заголовок
     */
    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * Проверяет, есть ли у медиа описание
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Проверяет, есть ли у медиа URL для доступа
     */
    public boolean hasFileUrl() {
        return fileUrl != null && !fileUrl.trim().isEmpty();
    }

    /**
     * Получает отображаемое имя типа медиа
     */
    public String getMediaTypeDisplayName() {
        return mediaType != null ? mediaType.getDisplayName() : null;
    }

    /**
     * Получает отображаемое имя типа файла
     */
    public String getFileTypeDisplayName() {
        return fileType != null ? fileType.getDisplayName() : null;
    }

    /**
     * Получает размер файла в человекочитаемом формате
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes == null) {
            return "Unknown";
        }
        
        long bytes = fileSizeBytes;
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Получает размеры изображения в формате строки
     */
    public String getDimensionsString() {
        if (!hasDimensions()) {
            return null;
        }
        return String.format("%dx%d", width, height);
    }

    /**
     * Получает длительность в человекочитаемом формате
     */
    public String getFormattedDuration() {
        if (!hasDuration()) {
            return null;
        }
        
        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    /**
     * Получает расширение файла
     */
    public String getFileExtension() {
        if (originalFilename == null) {
            return null;
        }
        
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == originalFilename.length() - 1) {
            return null;
        }
        
        return originalFilename.substring(lastDot + 1).toLowerCase();
    }

    /**
     * Валидирует DTO перед сохранением
     */
    public boolean isValid() {
        // Проверка обязательных полей
        if (mediaType == null) {
            return false;
        }
        
        if (fileType == null) {
            return false;
        }
        
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return false;
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        // Проверка логики полей
        if ((questId == null && levelId == null) || (questId != null && levelId != null)) {
            return false;
        }
        
        // Проверка длины полей
        if (originalFilename.length() > 255) {
            return false;
        }
        
        if (filePath.length() > 1000) {
            return false;
        }
        
        if (fileUrl != null && fileUrl.length() > 1000) {
            return false;
        }
        
        if (title != null && title.length() > 200) {
            return false;
        }
        
        if (description != null && description.length() > 1000) {
            return false;
        }
        
        if (mimeType != null && mimeType.length() > 100) {
            return false;
        }
        
        // Проверка числовых полей
        if (fileSizeBytes != null && fileSizeBytes <= 0) {
            return false;
        }
        
        if (width != null && width <= 0) {
            return false;
        }
        
        if (height != null && height <= 0) {
            return false;
        }
        
        if (durationSeconds != null && durationSeconds <= 0) {
            return false;
        }
        
        if (orderIndex != null && orderIndex < 0) {
            return false;
        }
        
        return true;
    }

    /**
     * Подготавливает DTO к сохранению (очистка данных)
     */
    public void prepareForSave() {
        if (originalFilename != null) {
            originalFilename = originalFilename.trim();
        }
        
        if (filePath != null) {
            filePath = filePath.trim();
        }
        
        if (fileUrl != null) {
            fileUrl = fileUrl.trim();
        }
        
        if (title != null) {
            title = title.trim();
        }
        
        if (description != null) {
            description = description.trim();
        }
        
        if (mimeType != null) {
            mimeType = mimeType.trim();
        }
        
        // Установка значений по умолчанию
        if (active == null) {
            active = true;
        }
        
        if (isCover == null) {
            isCover = false;
        }
        
        if (orderIndex == null) {
            orderIndex = 0;
        }
    }

    /**
     * Создает копию DTO
     */
    public QuestMediaDTO copy() {
        return QuestMediaDTO.builder()
                .questId(this.questId)
                .levelId(this.levelId)
                .mediaType(this.mediaType)
                .fileType(this.fileType)
                .originalFilename(this.originalFilename)
                .filePath(this.filePath)
                .fileUrl(this.fileUrl)
                .fileSizeBytes(this.fileSizeBytes)
                .mimeType(this.mimeType)
                .title(this.title)
                .description(this.description)
                .orderIndex(this.orderIndex)
                .active(this.active)
                .isCover(this.isCover)
                .width(this.width)
                .height(this.height)
                .durationSeconds(this.durationSeconds)
                .build();
    }
}