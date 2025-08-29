package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LevelProgressRepository extends JpaRepository<LevelProgress, Long> {

    @Query("select lp from LevelProgress lp where lp.session = :session and lp.level = :level")
    Optional<LevelProgress> findBySessionAndLevel(@Param("session") GameSession session,
                                                  @Param("level") Level level);

    // Текущий — тот, у которого closedAt is null (в проекте допускаем только один активный)
    @Query("select lp from LevelProgress lp " +
            "where lp.session = :session and lp.closedAt is null")
    Optional<LevelProgress> findCurrentBySession(@Param("session") GameSession session);

}
