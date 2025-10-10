package dn.quest.services.impl;

import dn.quest.model.entities.enums.ApplicantType;
import dn.quest.model.entities.enums.ParticipationStatus;
import dn.quest.model.entities.enums.QuestType;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
                .orElseThrow(() -> new EntityNotFoundException("Квест не найден"));

        // Проверка типа квеста
        if (quest.getType() == QuestType.SOLO && type != ApplicantType.USER) {
            throw new IllegalArgumentException("Этот квест — SOLO. Подайте заявку как USER.");
        }
        if (quest.getType() == QuestType.TEAM && type != ApplicantType.TEAM) {
            throw new IllegalArgumentException("Этот квест — TEAM. Подайте заявку как TEAM.");
        }

        if (type == ApplicantType.USER) {
            if (userIdOrNull == null) throw new IllegalArgumentException("Не указан пользователь для соло заявки");
            User user = userRepository.findById(userIdOrNull)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            return requestRepository.findByQuestAndUser(quest, user)
                    .map(req -> handleExistingRequest(req))
                    .orElseGet(() -> {
                        ParticipationRequest request = new ParticipationRequest();
                        request.setQuest(quest);
                        request.setUser(user);
                        request.setStatus(ParticipationStatus.PENDING);
                        request.setApplicantType(type);
                        request.setCreatedAt(Instant.now());
                        return requestRepository.save(request);
                    });

        } else { // TEAM
            if (teamIdOrNull == null) throw new IllegalArgumentException("Не указана команда для командной заявки");
            Team team = teamRepository.findById(teamIdOrNull)
                    .orElseThrow(() -> new EntityNotFoundException("Команда не найдена"));

            return requestRepository.findByQuestAndTeam(quest, team)
                    .map(req -> handleExistingRequest(req))
                    .orElseGet(() -> {
                        ParticipationRequest request = new ParticipationRequest();
                        request.setQuest(quest);
                        request.setTeam(team);
                        request.setStatus(ParticipationStatus.PENDING);
                        request.setApplicantType(type);
                        request.setCreatedAt(Instant.now());
                        return requestRepository.save(request);
                    });
        }
    }

    private ParticipationRequest handleExistingRequest(ParticipationRequest req) {
        switch (req.getStatus()) {
            case PENDING -> throw new RuntimeException("Заявка уже подана");
            case ACCEPTED -> throw new RuntimeException("Уже приняты на этот квест");
            case REJECTED, CANCELLED -> {
                req.setStatus(ParticipationStatus.PENDING);
                req.setCreatedAt(Instant.now());
                return requestRepository.save(req);
            }
        }
        throw new IllegalStateException("Неизвестный статус: " + req.getStatus());
    }

    @Override
    public ParticipationRequest changeStatus(Long teamId, ParticipationStatus newStatus) {
        ParticipationRequest teamRequest = requestRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + teamId));

        // Проверка прав: только автор квеста может менять статус заявки
        Quest quest = teamRequest.getQuest();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        // Предполагается, что username уникален и есть метод findByUsername в UserRepository
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("Текущий пользователь не найден: " + currentUsername));
        boolean isAuthor = quest.getAuthors().stream()
                .anyMatch(a -> a.getId().equals(currentUser.getId()));
        if (!isAuthor) {
            throw new SecurityException("Only quest author can change participation status");
        }

        teamRequest.setStatus(newStatus);
        if (newStatus != ParticipationStatus.PENDING) {
            teamRequest.setDecidedAt(Instant.now());
        }
        return requestRepository.save(teamRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequest> listByQuest(Long questId, ParticipationStatus status) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Квест не найден: " + questId));
        if (status != null) {
            return requestRepository.findByQuestAndStatus(quest, status);
        } else {
            // вернуть все заявки по квесту — добавим соответствующий метод в репозиторий
            return requestRepository.findByQuest(quest);
        }
    }

    @Override
    public void withdrawRequest(Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + requestId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("Текущий пользователь не найден: " + currentUsername));

        // если это командная заявка — только капитан команды может отзывать
        if (request.getApplicantType() == ApplicantType.TEAM) {
            Team team = request.getTeam();
            if (team == null) throw new IllegalStateException("Заявка не привязана к команде");
            if (team.getCaptain() == null || !team.getCaptain().getId().equals(currentUser.getId())) {
                throw new SecurityException("Only team captain can withdraw the request");
            }
        } else {
            // для соло-заявки — только сам пользователь
            if (request.getUser() == null || !request.getUser().getId().equals(currentUser.getId())) {
                throw new SecurityException("Only applicant can withdraw the request");
            }
        }

        if (request.getStatus() == ParticipationStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot withdraw an accepted request");
        }

        requestRepository.delete(request);
    }

}
