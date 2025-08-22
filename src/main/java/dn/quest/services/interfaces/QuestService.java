package dn.quest.services.interfaces;

import dn.quest.model.entities.enums.Difficulty;
import dn.quest.model.entities.enums.QuestType;
import dn.quest.model.entities.quest.Quest;

import java.time.Instant;
import java.util.List;

public interface QuestService {

    Quest create(String title,
                 String descriptionHtml,
                 Difficulty difficulty,
                 QuestType type,
                 Instant startAt);

    Quest update(Long questId,
                 String title,
                 String descriptionHtml,
                 Difficulty difficulty,
                 QuestType type,
                 Instant startAt,
                 Instant endAt,
                 Boolean published);

    void addAuthor(Long questId, Integer userId);
    void removeAuthor(Long questId, Integer userId);

    List<Quest> listActiveForMainPage(); // published=true & startAt<=now & endAt IS NULL
    Quest getById(Long id);

    // автогенерация номера квеста (sequence/сервис) — внутри create()

}
