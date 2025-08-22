package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeAttemptRepository extends JpaRepository<CodeAttempt, Long> {
    List<CodeAttempt> findTop10BySessionAndLevelOrderByCreatedAtDesc(GameSession session, Level level);
    List<CodeAttempt> findBySessionAndMatchedCode(GameSession session, Code matched);
}
