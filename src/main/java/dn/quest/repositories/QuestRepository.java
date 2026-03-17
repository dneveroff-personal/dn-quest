package dn.quest.repositories;

import dn.quest.model.entities.quest.Quest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    // Оптимизированные запросы с индексами
    List<Quest> findByEndAtIsNullOrderByStartAtAsc();
    
    @Query("SELECT q FROM Quest q WHERE q.endAt IS NULL ORDER BY q.startAt ASC")
    List<Quest> findActiveQuests();
    
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.endAt IS NULL ORDER BY q.startAt ASC")
    List<Quest> findPublishedActiveQuests();
    
    @Query("""
        SELECT q FROM Quest q
        WHERE q.published = true
          AND q.startAt <= :now
          AND q.endAt IS NULL
        ORDER BY q.startAt ASC
    """)
    List<Quest> findAvailableQuests(@Param("now") Instant now);
    
    // Запросы с пагинацией для больших объемов данных
    @Query("SELECT q FROM Quest q WHERE q.published = true ORDER BY q.startAt ASC")
    Page<Quest> findPublishedQuests(Pageable pageable);
    
    @Query("SELECT q FROM Quest q WHERE q.endAt IS NULL ORDER BY q.startAt ASC")
    Page<Quest> findActiveQuestsPaged(Pageable pageable);
    
    // Запросы с JOIN FETCH для избежания N+1 проблемы
    @Query("SELECT q FROM Quest q LEFT JOIN FETCH q.authors WHERE q.id = :id")
    Optional<Quest> findByIdWithAuthors(@Param("id") Long id);
    
    @Query("SELECT q FROM Quest q LEFT JOIN FETCH q.authors LEFT JOIN FETCH q.levels WHERE q.id = :id")
    Optional<Quest> findByIdWithAuthorsAndLevels(@Param("id") Long id);
    
    // Поиск по названию
    @Query("SELECT q FROM Quest q WHERE q.published = true AND LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY q.startAt ASC")
    List<Quest> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    // Подсчет количества квестов
    @Query("SELECT COUNT(q) FROM Quest q WHERE q.published = true AND q.endAt IS NULL")
    long countPublishedActiveQuests();
    
    // Запросы для статистики
    @Query("SELECT q FROM Quest q WHERE q.createdBy = :userId ORDER BY q.createdAt DESC")
    List<Quest> findByCreatedBy(@Param("userId") Long userId);
}