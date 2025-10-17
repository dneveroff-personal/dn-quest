package dn.quest.model.dto;

import dn.quest.model.entities.enums.CodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Унифицированное представление всех кодов уровня:
 * обычных (NORMAL), бонусных (BONUS) и штрафных (PENALTY).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeViewDTO {
    private Long id;
    private Long levelId;
    private CodeType type;         // NORMAL, BONUS, PENALTY
    private Integer sectorNo;      // Только для NORMAL
    private String value;          // Сам код
    private Integer shiftSeconds;  // Для BONUS/PENALTY
    private boolean closed;        // Введён ли код командой
    private String matchedCodeValue; // Для наглядности, если введён
}
