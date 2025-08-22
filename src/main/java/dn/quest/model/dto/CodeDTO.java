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
    private CodeType type;
    private Integer sectorNo;   // nullable
    private String value;       // normalized value stored
    private int shiftSeconds;

}