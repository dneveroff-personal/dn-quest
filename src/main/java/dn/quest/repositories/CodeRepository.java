package dn.quest.repositories;

import dn.quest.model.entities.enums.CodeType;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.Code;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeRepository extends JpaRepository<Code, Long> {
    List<Code> findByLevelAndType(Level level, CodeType type);
    List<Code> findByLevel(Level level);
}
