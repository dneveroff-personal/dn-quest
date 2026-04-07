package dn.quest.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа на поисковый запрос файлов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchResponseDTO {

    /**
     * Список файлов
     */
    private List<FileResponseDTO> files;

    /**
     * Номер текущей страницы
     */
    private int currentPage;

    /**
     * Размер страницы
     */
    private int pageSize;

    /**
     * Общее количество элементов
     */
    private long totalElements;

    /**
     * Общее количество страниц
     */
    private int totalPages;

    /**
     * Является ли первая страница
     */
    private boolean isFirst;

    /**
     * Является ли последняя страница
     */
    private boolean isLast;

    /**
     * Есть ли следующая страница
     */
    private boolean hasNext;

    /**
     * Есть ли предыдущая страница
     */
    private boolean hasPrevious;

    /**
     * Общий размер файлов в байтах
     */
    private Long totalSizeBytes;

    /**
     * Общий размер файлов в человекочитаемом формате
     */
    private String formattedTotalSize;

    /**
     * Время выполнения запроса в миллисекундах
     */
    private Long processingTimeMs;

    /**
     * Получить отформатированный общий размер
     */
    public static String formatTotalSize(Long totalSizeBytes) {
        if (totalSizeBytes == null || totalSizeBytes == 0) {
            return "0 B";
        }
        return FileResponseDTO.formatFileSize(totalSizeBytes);
    }
}