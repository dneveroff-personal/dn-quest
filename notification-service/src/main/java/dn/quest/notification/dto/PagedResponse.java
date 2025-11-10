package dn.quest.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для пагинированного ответа
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    /**
     * Список элементов
     */
    private List<T> content;

    /**
     * Номер текущей страницы
     */
    private int page;

    /**
     * Размер страницы
     */
    private int size;

    /**
     * Общее количество элементов
     */
    private long totalElements;

    /**
     * Общее количество страниц
     */
    private int totalPages;

    /**
     * Количество элементов на текущей странице
     */
    private int numberOfElements;

    /**
     * Есть ли следующая страница
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    /**
     * Есть ли предыдущая страница
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * Это первая страница
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * Это последняя страница
     */
    public boolean isLast() {
        return page == totalPages - 1;
    }

    /**
     * Пустой ли ответ
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}