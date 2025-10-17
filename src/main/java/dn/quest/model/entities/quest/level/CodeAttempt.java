package dn.quest.model.entities.quest.level;

import dn.quest.model.entities.enums.AttemptResult;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name="game_code_attempts",
        indexes = {
                @Index(name="idx_attempt_session_level_time", columnList="session_id,level_id,created_at")
        })
public class CodeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="session_id", nullable=false)
    private GameSession session;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="level_id", nullable=false)
    private Level level;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;                       // кто ввёл (в команде)

    @Column(nullable=false, length=180)
    private String submittedRaw;

    @Column(nullable=false, length=180)
    private String submittedNormalized;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private AttemptResult result;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="matched_code_id")
    private Code matchedCode;

    @Column(name="matched_sector_no")
    private Integer matchedSectorNo;

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();

    private String ip;
    private String userAgent;
}
