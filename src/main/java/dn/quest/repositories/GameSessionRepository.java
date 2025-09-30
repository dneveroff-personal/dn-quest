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

    List<GameSession> findByQuest(Quest quest);

    List<GameSession> findByUser(User user);

    List<GameSession> findByTeam(Team team);

    Optional<GameSession> findByQuestAndUser(Quest quest, User user);

    Optional<GameSession> findByQuestAndTeam(Quest quest, Team team);

    // ----- новые методы: поиск именно ACTIVE сессии
    Optional<GameSession> findByQuestAndTeamAndStatus(Quest quest, Team team, SessionStatus status);
    Optional<GameSession> findByQuestAndUserAndStatus(Quest quest, User user, SessionStatus status);
}
