package dn.quest.model.entities.user;

import dn.quest.model.entities.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_users_username", columnNames = "username"),
                @UniqueConstraint(name="uk_users_email", columnNames = "email")
        })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 255)
    private String email;

    @Column(length = 128)
    private String publicName;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.PLAYER;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    //private String authority; ? может не убирать а оставить для ролей?
}
