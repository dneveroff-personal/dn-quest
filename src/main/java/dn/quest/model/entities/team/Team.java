package dn.quest.model.entities.team;

import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "teams", uniqueConstraints = @UniqueConstraint(name="uk_team_name", columnNames="name"))
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id", nullable = false)
    private User captain;

    private String logoUrl;

    private Instant createdAt = Instant.now();

}
