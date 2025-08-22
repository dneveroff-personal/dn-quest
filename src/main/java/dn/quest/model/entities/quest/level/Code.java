package dn.quest.model.entities.quest.level;

import dn.quest.model.entities.enums.CodeType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="level_codes",
        indexes = {
                @Index(name="idx_code_level", columnList="level_id"),
                @Index(name="idx_code_type_sector", columnList="type,sector_no")
        })
public class Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="level_id", nullable=false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private CodeType type = CodeType.NORMAL;

    // для NORMAL-кодов: номер сектора (1..K). Для BONUS/PENALTY может быть null.
    @Column(name="sector_no")
    private Integer sectorNo;

    // нормализованное значение кода (toLowerCase)
    @Column(nullable=false, length=200)
    private String value;

    // сдвиг времени в секундах: >0 для бонуса, <0 для штрафа, 0 — для NORMAL
    @Column(nullable=false)
    private int shiftSeconds = 0;

}
