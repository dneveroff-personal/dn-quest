package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelProgressRepository extends JpaRepository<LevelProgress, Long> {
    Optional<LevelProgress> findBySessionAndLevel(GameSession session, Level level);
    Optional<LevelProgress> findCurrentBySession(GameSession session);
}
