package dn.quest.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO для постраничной навигации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDTO<T> {
    
    /**
     * Список элементов на текущей странице
     */
    private List<T> content;
    
    /**
     * Номер текущей страницы (начиная с 0)
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
     * Первый ли элемент на странице
     */
    private boolean first;
    
    /**
     * Последний ли элемент на странице
     */
    private boolean last;
    
    /**
     * Есть ли следующая страница
     */
    private boolean hasNext;
    
    /**
     * Есть ли предыдущая страница
     */
    private boolean hasPrevious;
    
    /**
     * Количество элементов на текущей странице
     */
    private int numberOfElements;
    
    /**
     * Пустая ли страница
     */
    private boolean empty;
    
    /**
     * Создает пустую страницу
     */
    public static <T> PaginationDTO<T> empty() {
        return PaginationDTO.<T>builder()
                .content(List.of())
                .page(0)
                .size(0)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(0)
                .empty(true)
                .build();
    }
    
    /**
     * Создает страницу с данными
     */
    public static <T> PaginationDTO<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1;
        
        return PaginationDTO.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(first)
                .last(last)
                .hasNext(!last)
                .hasPrevious(!first)
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .build();
    }
    
    /**
     * Создает страницу из Spring Page
     */
    public static <T> PaginationDTO<T> of(Page<T> page) {
        return PaginationDTO.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
    
    /**
     * Создает страницу из Spring Page с функцией преобразования
     */
    public static <T, R> PaginationDTO<R> of(Page<T> page, java.util.function.Function<T, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).toList();
        return PaginationDTO.<R>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
}