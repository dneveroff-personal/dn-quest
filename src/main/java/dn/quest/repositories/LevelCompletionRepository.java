package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LevelCompletionRepository extends JpaRepository<LevelCompletion, Long> {

    @Query("select lc from LevelCompletion lc " +
            "where lc.session = :session and lc.level = :level")
    Optional<LevelCompletion> findBySessionAndLevel(@Param("session") GameSession session,
                                                    @Param("level") Level level);

    @Query("select lc from LevelCompletion lc " +
            "where lc.session = :session " +
            "order by lc.level.orderIndex asc")
    List<LevelCompletion> findBySessionOrdered(@Param("session") GameSession session);

    @Query("select lc from LevelCompletion lc " +
            "where lc.level.quest = :quest " +
            "order by lc.passTime asc")
    List<LevelCompletion> findByQuest(@Param("quest") Quest quest);

}
