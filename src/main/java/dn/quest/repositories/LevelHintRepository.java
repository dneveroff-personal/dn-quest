package dn.quest.repositories;

import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LevelHintRepository extends JpaRepository<LevelHint, Long> {

    List<LevelHint> findByLevelOrderByOrderIndexAsc(Level level);

    @Query("select coalesce(max(h.orderIndex), 0) from LevelHint h where h.level.id = :levelId")
    Optional<Integer> findMaxOrderIndexByLevel(@Param("levelId") Long levelId);
}
