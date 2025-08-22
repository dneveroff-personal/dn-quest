package dn.quest.controllers;

import dn.quest.model.dto.CodeAttemptDTO;
import dn.quest.model.dto.LevelDTO;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.LevelProgress;
import dn.quest.repositories.GameSessionRepository;
import dn.quest.repositories.LevelProgressRepository;
import dn.quest.repositories.QuestRepository;
import dn.quest.services.AttemptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping(Routes.QUESTS)
@RequiredArgsConstructor
public class QuestController implements Routes {

    private final GameSessionRepository sessionRepo;
    private final QuestRepository questRepo;

/*    @PostMapping(Routes.QUEST_START)
    public ResponseEntity<Long> startSession(@PathVariable Long questId, Principal principal) {
        // проверка прав/типа, создание GameSession, создание LevelProgress для первого уровня и возврат sessionId

    }*/

}
