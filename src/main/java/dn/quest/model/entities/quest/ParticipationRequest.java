package dn.quest.model.entities.quest;

import dn.quest.model.entities.enums.ApplicantType;
import dn.quest.model.entities.enums.ParticipationStatus;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name="participation_requests")
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="quest_id", nullable=false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=8)
    private ApplicantType applicantType;   // USER / TEAM

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;                     // если SOLO

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="team_id")
    private Team team;                     // если TEAM

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=12)
    private ParticipationStatus status = ParticipationStatus.PENDING;

    private Instant createdAt = Instant.now();
    private Instant decidedAt;

}
