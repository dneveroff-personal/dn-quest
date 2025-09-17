package dn.quest.model.dto;

import dn.quest.model.entities.enums.CodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeDTO {

    private Long id;
    private Long levelId;
    private CodeType type; // NORMAL / BONUS / PENALTY
    private Integer sectorNo;                // для NORMAL (1..K), иначе null
    private String value;                    // код (уже нормализованный на бэке)
    private Integer shiftSeconds;            // >0 бонус, <0 штраф, 0 normal

}