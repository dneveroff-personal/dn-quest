package dn.quest.model.entities.quest.level;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

@Data
@Entity
@Table(name="game_level_completions",
        uniqueConstraints = @UniqueConstraint(name="uk_session_level_complete", columnNames={"session_id","level_id"}))
public class LevelCompletion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="session_id", nullable=false)
    private GameSession session;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="level_id", nullable=false)
    private Level level;

    // кто ввёл последний правильный код, закрывший уровень
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="passed_by_user_id")
    private User passedByUser;

    @Column(nullable=false)
    private Instant passTime;                 // момент прохождения уровня

    @Column(nullable=false)
    private long durationSec;                 // (passTime - levelStartedAt)

    @Column(nullable=false)
    private int bonusOnLevelSec = 0;

    @Column(nullable=false)
    private int penaltyOnLevelSec = 0;

    // удобный геттер
    public Duration duration() { return Duration.ofSeconds(durationSec); }

    @PrePersist
    public void prePersist() {
        if (passTime == null) {
            passTime = Instant.now();
        }
    }
}
