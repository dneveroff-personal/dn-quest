package dn.quest.services.impl;

import dn.quest.model.entities.enums.ApplicantType;
import dn.quest.model.entities.enums.ParticipationStatus;
import dn.quest.model.entities.quest.ParticipationRequest;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.ParticipationRequestRepository;
import dn.quest.repositories.QuestRepository;
import dn.quest.repositories.TeamRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.ParticipationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRequestRepository requestRepository;
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Override
    public ParticipationRequest createRequest(Long questId, ApplicantType type, Long userIdOrNull, Long teamIdOrNull) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("Квест не найден"));

        if (userIdOrNull != null) {
            User user = userRepository.findById(userIdOrNull.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            // Проверка на существующую заявку
            if (!requestRepository.findByQuestAndUser(quest, user).isEmpty()) {
                throw new RuntimeException("Пользователь уже подал заявку на этот квест");
            }

            ParticipationRequest request = new ParticipationRequest();
            request.setQuest(quest);
            request.setUser(user);
            request.setStatus(ParticipationStatus.PENDING);
            request.setApplicantType(ApplicantType.USER);
            request.setCreatedAt(Instant.now());

            return requestRepository.save(request);

        } else if (teamIdOrNull != null) {
            Team team = teamRepository.findById(teamIdOrNull)
                    .orElseThrow(() -> new EntityNotFoundException("Команда не найдена"));

            // Проверка на существующую заявку
            if (!requestRepository.findByQuestAndTeam(quest, team).isEmpty()) {
                throw new RuntimeException("Команда уже подала заявку на этот квест");
            }

            ParticipationRequest request = new ParticipationRequest();
            request.setQuest(quest);
            request.setTeam(team);
            request.setStatus(ParticipationStatus.PENDING);
            request.setApplicantType(ApplicantType.TEAM);
            request.setCreatedAt(Instant.now());

            return requestRepository.save(request);

        } else {
            throw new IllegalArgumentException("Неизвестный тип участника");
        }
    }

    @Override
    public ParticipationRequest changeStatus(Long requestId, ParticipationStatus newStatus) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + requestId));
        request.setStatus(newStatus);
        return requestRepository.save(request);
    }

    @Override
    public List<ParticipationRequest> listByQuest(Long questId, ParticipationStatus status) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Квест не найден: " + questId));
        if (status != null) {
            return requestRepository.findByQuestAndStatus(quest, status);
        } else {
            return requestRepository.findByQuestAndStatus(quest, ParticipationStatus.PENDING); // можно вернуть все PENDING по умолчанию
        }
    }
}
