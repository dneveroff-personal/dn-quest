package dn.quest.model.entities.team;

import dn.quest.model.entities.enums.TeamRole;
import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(name="uk_team_user", columnNames={"team_id","user_id"}))
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="team_id", nullable=false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private TeamRole role = TeamRole.MEMBER;

    private Instant joinedAt = Instant.now();

}
