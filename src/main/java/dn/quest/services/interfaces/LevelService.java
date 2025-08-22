package dn.quest.services.interfaces;

import dn.quest.model.entities.enums.CodeType;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.Level;

public interface LevelService {

    Level addLevel(Long questId, int orderIndex, String title, String descriptionHtml,
                   Integer apTime, Integer requiredSectors);

    Level updateLevel(Long levelId, String title, String descriptionHtml,
                      Integer apTime, Integer requiredSectors, Integer newOrderIndex);

    void deleteLevel(Long levelId);

    // Работа с кодами
    Code addCode(Long levelId, CodeType type, Integer sectorNo, String value, int shiftSeconds);
    void deleteCode(Long codeId);

}
