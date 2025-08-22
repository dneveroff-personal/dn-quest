package dn.quest.repositories;

import dn.quest.model.entities.enums.SessionStatus;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findByQuestAndStatus(Quest quest, SessionStatus status);
    Optional<GameSession> findByQuestAndTeamAndStatus(Quest quest, Team team, SessionStatus status);
    Optional<GameSession> findByQuestAndUserAndStatus(Quest quest, User user, SessionStatus status);
}
