package dn.quest.repositories;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Level;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CodeAttemptRepository extends JpaRepository<CodeAttempt, Long> {

    @Query("select ca from CodeAttempt ca " +
            "where ca.session = :session and ca.level = :level " +
            "order by ca.createdAt desc")
    List<CodeAttempt> findLastAttempts(@Param("session") GameSession session,
                                       @Param("level") Level level,
                                       Pageable pageable);

    @Query("select ca from CodeAttempt ca " +
            "where ca.session = :session and ca.matchedCode = :code")
    List<CodeAttempt> findBySessionAndMatchedCode(@Param("session") GameSession session,
                                                  @Param("code") Code matched);

    // сколько УНИКАЛЬНЫХ сектор-номеров закрыто принятыми NORMAL-кодами
    @Query("select count(distinct ca.matchedSectorNo) " +
            "from CodeAttempt ca " +
            "where ca.session = :session and ca.level = :level " +
            "and ca.result in ('ACCEPTED_NORMAL','ACCEPTED_BONUS','ACCEPTED_PENALTY') " + // принят любой, но считаем только там, где сектор есть
            "and ca.matchedCode.type = dn.quest.model.entities.enums.CodeType.NORMAL " +
            "and ca.matchedSectorNo is not null")
    long countDistinctClosedSectors(@Param("session") GameSession session,
                                    @Param("level") Level level);

}
