package dn.quest.model.entities.quest;

import dn.quest.model.entities.enums.Difficulty;
import dn.quest.model.entities.enums.QuestType;
import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "quests")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // «Номер квеста» — можно автогенерить в сервисе/триггером, держим поле
    @Column(name="quest_number", unique=true)
    private Long number;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private Difficulty difficulty;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=8)
    private QuestType type = QuestType.TEAM;       // SOLO / TEAM

    @Column(nullable=false, length=300)
    private String title;

    @Lob
    private String descriptionHtml;

    @ManyToMany
    @JoinTable(
            name = "quest_authors",
            joinColumns = @JoinColumn(name = "quest_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> authors = new HashSet<>();

    private Instant startAt;               // дата начала
    private Instant endAt;                 // дата окончания (NULL = активен)

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @Column(nullable=false)
    private boolean published = false;

}
