package dn.quest.model.entities.quest.level;

import dn.quest.model.entities.quest.Quest;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="levels",
        uniqueConstraints = @UniqueConstraint(name="uk_level_order_in_quest", columnNames={"quest_id","order_index"}))
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="quest_id", nullable=false)
    private Quest quest;

    @Column(name="order_index", nullable=false)
    private Integer orderIndex;                 // порядковый номер уровня (1..N)

    @Column(nullable=false, length=200)
    private String title;

    @Lob
    private String descriptionHtml;

    // АП на этот уровень для сессии (сек), null — без ограничения
    private Integer apTime;

    // сколько разных «секторов» нужно закрыть правильными кодами NORMAL
    @Column(nullable=false)
    private Integer requiredSectors = 0;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LevelCompletion> completions = new HashSet<>();

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CodeAttempt> attempts = new HashSet<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;
}
