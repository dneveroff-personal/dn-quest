package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

}
