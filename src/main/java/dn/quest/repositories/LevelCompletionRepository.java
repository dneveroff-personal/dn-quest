package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LevelCompletionRepository extends JpaRepository<LevelCompletion, Long> {
    Optional<LevelCompletion> findBySessionAndLevel(GameSession session, Level level);
    List<LevelCompletion> findBySessionOrderByLevel_OrderIndexAsc(GameSession session);
}
