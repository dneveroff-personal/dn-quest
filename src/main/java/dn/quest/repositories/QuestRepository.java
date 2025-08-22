package dn.quest.repositories;

import dn.quest.model.entities.quest.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByEndAtIsNullOrderByStartAtAsc();
    @Query("""
        SELECT q FROM Quest q
        WHERE q.published = true
          AND q.startAt <= :now
          AND q.endAt IS NULL
        ORDER BY q.startAt ASC
    """)
    List<Quest> findAvailableQuests(@Param("now") LocalDateTime now);}