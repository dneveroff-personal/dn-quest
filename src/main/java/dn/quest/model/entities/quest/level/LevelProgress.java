package dn.quest.model.entities.quest.level;

import dn.quest.model.entities.quest.GameSession;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name="level_progress",
        uniqueConstraints = @UniqueConstraint(name="uk_session_level", columnNames={"session_id","level_id"}))
public class LevelProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="session_id", nullable=false)
    private GameSession session;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="level_id", nullable=false)
    private Level level;

    private Instant startedAt = Instant.now();
    private Instant completedAt;

    @Column(nullable=false)
    private int completedSectors = 0;

    // helpers: isCompleted() -> completedAt != null
}
