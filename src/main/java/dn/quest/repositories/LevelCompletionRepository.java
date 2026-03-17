package dn.quest.repositories;

import dn.quest.model.dto.LevelCompletionDTO;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.quest.level.LevelCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LevelCompletionRepository extends JpaRepository<LevelCompletion, Long> {

    @Query("select lc from LevelCompletion lc " +
            "where lc.level.quest = :quest " +
            "order by lc.passTime asc")
    List<LevelCompletion> findByQuest(@Param("quest") Quest quest);

    @Query("""
    select new dn.quest.model.dto.LevelCompletionDTO(
            l.id,
            l.title,
            s.team.id,
            coalesce(s.team.name, '-'),
            u.id,
            coalesce(u.publicName, '-'),
            lc.passTime,
            '00:00:00',
            lc.bonusOnLevelSec,
            lc.penaltyOnLevelSec
        )
        from LevelCompletion lc
        join lc.level l
        join lc.session s
        left join s.team
        left join lc.passedByUser u
        where l.quest.id = :questId
        order by lc.passTime asc
    """)
    List<LevelCompletionDTO> findLeaderboardByQuestId(@Param("questId") Long questId);


    @Query("select count(distinct lc.session.id) from LevelCompletion lc where lc.level.quest.id = :questId")
    long countDistinctSessionsByQuestId(@Param("questId") Long questId);

    @Query("select coalesce(avg(lc.durationSec),0) from LevelCompletion lc where lc.level.quest.id = :questId")
    double averageDurationSecByQuestId(@Param("questId") Long questId);

}
