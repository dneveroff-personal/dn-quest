package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.Level;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CodeAttemptRepository extends JpaRepository<CodeAttempt, Long> {

    @Query("SELECT ca FROM CodeAttempt ca " +
            "WHERE ca.session.id = :sessionId AND ca.level.id = :levelId " +
            "ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findLastAttempts(Long sessionId, Long levelId, Pageable pageable);

    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.matchedCode = :code")
    List<CodeAttempt> findBySessionAndMatchedCode(GameSession session, Code code);

    @Query("SELECT COUNT(DISTINCT ca.matchedSectorNo) FROM CodeAttempt ca " +
            "WHERE ca.session = :session AND ca.level = :level AND ca.result IN ('ACCEPTED_NORMAL', 'ACCEPTED_BONUS', 'ACCEPTED_PENALTY')")
    long countDistinctClosedSectors(GameSession session, Level level);

}
