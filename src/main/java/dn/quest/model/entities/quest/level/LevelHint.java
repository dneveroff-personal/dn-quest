package dn.quest.model.entities.quest.level;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "level_hints",
        uniqueConstraints = @UniqueConstraint(name = "uk_level_hint_order", columnNames = {"level_id","order_index"}))
public class LevelHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ссылка на уровень
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    // смещение от startedAt (в секундах) после которого подсказка становится доступна
    @Column(name = "offset_sec", nullable = false)
    private Integer offsetSec = 0;

    // текст подсказки (html/plain)
    @Lob
    private String text;

    // порядок подсказок (1..N)
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 1;
}
