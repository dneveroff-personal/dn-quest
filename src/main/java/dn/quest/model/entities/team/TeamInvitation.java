package dn.quest.model.entities.team;

import dn.quest.model.entities.enums.InvitationStatus;
import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "team_invitations",
        uniqueConstraints = @UniqueConstraint(name="uk_team_user_invite", columnNames={"team_id","user_id"}))
@Data
public class TeamInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="team_id", nullable=false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private InvitationStatus status = InvitationStatus.PENDING;

    private Instant createdAt = Instant.now();
}
