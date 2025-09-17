package dn.quest.repositories;

import dn.quest.model.entities.enums.CodeType;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CodeRepository extends JpaRepository<Code, Long> {
    @Query("select c from Code c where c.level = :level")
    List<Code> findByLevel(@Param("level") Level level);

    @Query("select c from Code c where c.level = :level and c.type = :type")
    List<Code> findByLevelAndType(@Param("level") Level level, @Param("type") CodeType type);

    @Query("select c from Code c where c.level = :level and c.value = :normalized")
    Optional<Code> findByLevelAndNormalized(@Param("level") Level level,
                                            @Param("normalized") String normalized);

}
