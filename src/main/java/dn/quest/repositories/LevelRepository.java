package dn.quest.repositories;

import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.quest.level.Level;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {

    @Query("select l from Level l where l.quest = :quest order by l.orderIndex asc")
    List<Level> findAllOrdered(@Param("quest") Quest quest);

    @Query("select l from Level l where l.quest = :quest order by l.orderIndex asc")
    List<Level> findFirstInQuest(@Param("quest") Quest quest, Pageable pageable);

    default Level findFirstInQuest(Quest quest) {
        List<Level> list = findFirstInQuest(quest, org.springframework.data.domain.PageRequest.of(0, 1));
        return list.isEmpty() ? null : list.get(0);
    }

    @Query("select l from Level l " +
            "where l.quest = :quest and l.orderIndex > :orderIndex " +
            "order by l.orderIndex asc")
    List<Level> findNext(@Param("quest") Quest quest,
                         @Param("orderIndex") Integer orderIndex,
                         Pageable pageable);

    default Level findNext(Quest quest, Integer orderIndex) {
        List<Level> list = findNext(quest, orderIndex, org.springframework.data.domain.PageRequest.of(0, 1));
        return list.isEmpty() ? null : list.get(0);
    }
}
