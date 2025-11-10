package dn.quest.gameengine.controller;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import dn.quest.gameengine.entity.enums.SessionStatus;
import dn.quest.gameengine.service.GameSessionService;
import dn.quest.gameengine.dto.GameSessionDTO;
import dn.quest.gameengine.dto.CreateGameSessionRequest;
import dn.quest.gameengine.dto.UpdateGameSessionRequest;
import dn.quest.gameengine.dto.SubmitCodeRequest;
import dn.quest.gameengine.dto.SubmitCodeResponse;
import dn.quest.shared.dto.BaseDTO;
import dn.quest.shared.dto.PaginationDTO;
import dn.quest.shared.dto.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер для управления игровыми сессиями
 */
@Slf4j
@RestController
@RequestMapping("/api/game/sessions")
@RequiredArgsConstructor
@Tag(name = "Game Session Management", description = "API для управления игровыми сессиями")
public class GameSessionController {

    private final GameSessionService gameSessionService;

    @Operation(summary = "Получить все игровые сессии", description = "Возвращает список всех игровых сессий с пагинацией")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение списка сессий",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Неверные параметры запроса",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping
    public ResponseEntity<PaginationDTO<GameSessionDTO>> getAllSessions(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Фильтр по статусу") @RequestParam(required = false) SessionStatus status,
            @Parameter(description = "Фильтр по ID квеста") @RequestParam(required = false) Long questId,
            @Parameter(description = "Фильтр по ID пользователя") @RequestParam(required = false) Long userId,
            @Parameter(description = "Фильтр по ID команды") @RequestParam(required = false) Long teamId,
            @Parameter(description = "Начальная дата") @RequestParam(required = false) Instant startDate,
            @Parameter(description = "Конечная дата") @RequestParam(required = false) Instant endDate
    ) {
        log.info("Getting all sessions with filters - page: {}, size: {}, status: {}, questId: {}", 
                page, size, status, questId);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<GameSession> sessions;
        if (hasFilters(status, questId, userId, teamId, startDate, endDate)) {
            sessions = gameSessionService.getSessionsWithFilters(
                status, questId, userId, teamId, startDate, endDate, pageable);
        } else {
            sessions = gameSessionService.getAllSessions(pageable);
        }

        PaginationDTO<GameSessionDTO> response = PaginationDTO.of(sessions, this::convertToDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Создать новую игровую сессию", description = "Создает новую игровую сессию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Сессия успешно создана",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GameSessionDTO> createSession(
            @Valid @RequestBody CreateGameSessionRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Creating new game session by user: {}", currentUser.getId());

        GameSession session = convertToEntity(request);
        session.setOwner(currentUser);

        GameSession createdSession = gameSessionService.createSession(session);
        GameSessionDTO response = convertToDTO(createdSession);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить игровую сессию по ID", description = "Возвращает详细信息 игровой сессии")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сессия найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<GameSessionDTO> getSessionById(
            @Parameter(description = "ID сессии") @PathVariable Long id) {
        log.info("Getting game session by ID: {}", id);

        Optional<GameSession> session = gameSessionService.getSessionById(id);
        if (session.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GameSessionDTO response = convertToDTO(session.get());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновить игровую сессию", description = "Обновляет данные игровой сессии")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сессия успешно обновлена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GameSessionDTO> updateSession(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @Valid @RequestBody UpdateGameSessionRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Updating game session {} by user: {}", id, currentUser.getId());

        Optional<GameSession> existingSession = gameSessionService.getSessionById(id);
        if (existingSession.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GameSession session = existingSession.get();
        updateSessionFromRequest(session, request);

        GameSession updatedSession = gameSessionService.updateSession(session);
        GameSessionDTO response = convertToDTO(updatedSession);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить игровую сессию", description = "Удаляет игровую сессию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Сессия успешно удалена"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteSession(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Deleting game session {} by user: {}", id, currentUser.getId());

        Optional<GameSession> existingSession = gameSessionService.getSessionById(id);
        if (existingSession.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Проверка прав на удаление
        GameSession session = existingSession.get();
        if (!canModifySession(session, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        gameSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Запустить игровую сессию", description = "Запускает игровую сессию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сессия успешно запущена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Невозможно запустить сессию",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GameSessionDTO> startSession(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Starting game session {} by user: {}", id, currentUser.getId());

        if (!gameSessionService.canStartSession(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        GameSession startedSession = gameSessionService.startSession(id, currentUser);
        GameSessionDTO response = convertToDTO(startedSession);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Приостановить игровую сессию", description = "Приостанавливает игровую сессию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сессия успешно приостановлена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Невозможно приостановить сессию",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/pause")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GameSessionDTO> pauseSession(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Pausing game session {} by user: {}", id, currentUser.getId());

        if (!gameSessionService.canPauseSession(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        GameSession pausedSession = gameSessionService.pauseSession(id, currentUser);
        GameSessionDTO response = convertToDTO(pausedSession);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Возобновить игровую сессию", description = "Возобновляет игровую сессию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сессия успешно возобновлена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Невозможно возобновить сессию",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/resume")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GameSessionDTO> resumeSession(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Resuming game session {} by user: {}", id, currentUser.getId());

        if (!gameSessionService.canResumeSession(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        GameSession resumedSession = gameSessionService.resumeSession(id, currentUser);
        GameSessionDTO response = convertToDTO(resumedSession);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Завершить игровую сессию", description = "Завершает игровую сессию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сессия успешно завершена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Невозможно завершить сессию",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GameSessionDTO> finishSession(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Finishing game session {} by user: {}", id, currentUser.getId());

        if (!gameSessionService.canFinishSession(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        GameSession finishedSession = gameSessionService.finishSession(id, currentUser);
        GameSessionDTO response = convertToDTO(finishedSession);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить текущий уровень сессии", description = "Возвращает информацию о текущем уровне")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение информации",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}/current-level")
    public ResponseEntity<BaseDTO> getCurrentLevel(
            @Parameter(description = "ID сессии") @PathVariable Long id) {
        log.info("Getting current level for session: {}", id);

        Optional<Long> currentLevelId = gameSessionService.getCurrentLevelId(id);
        if (currentLevelId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BaseDTO response = BaseDTO.builder()
            .id(currentLevelId.get())
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Отправить код для проверки", description = "Проверяет код в текущем уровне")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Код успешно проверен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmitCodeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/submit-code")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubmitCodeResponse> submitCode(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @Valid @RequestBody SubmitCodeRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Submitting code for session {} by user: {}", id, currentUser.getId());

        // TODO: Реализация проверки кода через CodeAttemptService
        SubmitCodeResponse response = SubmitCodeResponse.builder()
            .success(false)
            .message("Code submission not implemented yet")
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить прогресс сессии", description = "Возвращает прогресс сессии")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение прогресса",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}/progress")
    public ResponseEntity<BaseDTO> getSessionProgress(
            @Parameter(description = "ID сессии") @PathVariable Long id) {
        log.info("Getting progress for session: {}", id);

        // TODO: Реализация получения прогресса через LevelProgressService
        BaseDTO response = BaseDTO.builder()
            .id(id)
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить попытки сессии", description = "Возвращает список попыток в сессии")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение попыток",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}/attempts")
    public ResponseEntity<PaginationDTO<BaseDTO>> getSessionAttempts(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        log.info("Getting attempts for session: {}", id);

        // TODO: Реализация получения попыток через CodeAttemptService
        Page<BaseDTO> emptyPage = Page.empty();
        PaginationDTO<BaseDTO> response = PaginationDTO.of(emptyPage, attempt -> BaseDTO.builder().id(attempt.getId()).build());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить лидерборд сессии", description = "Возвращает лидерборд сессии")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение лидерборда",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<BaseDTO> getSessionLeaderboard(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        log.info("Getting leaderboard for session: {}", id);

        // TODO: Реализация получения лидерборда через LeaderboardService
        BaseDTO response = BaseDTO.builder()
            .id(id)
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Присоединиться к сессии", description = "Присоединяет пользователя к сессии")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное присоединение",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameSessionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Невозможно присоединиться",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GameSessionDTO> joinSession(
            @Parameter(description = "ID сессии") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("User {} joining session: {}", currentUser.getId(), id);

        if (!gameSessionService.canJoinSession(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        GameSession updatedSession = gameSessionService.joinSession(id, currentUser);
        GameSessionDTO response = convertToDTO(updatedSession);

        return ResponseEntity.ok(response);
    }

    // Приватные вспомогательные методы

    private boolean hasFilters(SessionStatus status, Long questId, Long userId, Long teamId, Instant startDate, Instant endDate) {
        return status != null || questId != null || userId != null || teamId != null || startDate != null || endDate != null;
    }

    private boolean canModifySession(GameSession session, User currentUser) {
        return session.getOwner().equals(currentUser) || hasAdminRole(currentUser);
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
            .anyMatch(role -> role.name().equals("ADMIN") || role.name().equals("MODERATOR"));
    }

    private GameSession convertToEntity(CreateGameSessionRequest request) {
        // TODO: Реализация конвертации из DTO в Entity
        return GameSession.builder()
            .name(request.getName())
            .description(request.getDescription())
            .maxParticipants(request.getMaxParticipants())
            .build();
    }

    private void updateSessionFromRequest(GameSession session, UpdateGameSessionRequest request) {
        if (request.getName() != null) {
            session.setName(request.getName());
        }
        if (request.getDescription() != null) {
            session.setDescription(request.getDescription());
        }
        if (request.getMaxParticipants() != null) {
            session.setMaxParticipants(request.getMaxParticipants());
        }
    }

    private GameSessionDTO convertToDTO(GameSession session) {
        return GameSessionDTO.builder()
            .id(session.getId())
            .name(session.getName())
            .description(session.getDescription())
            .status(session.getStatus())
            .ownerId(session.getOwner() != null ? session.getOwner().getId() : null)
            .questId(session.getQuest() != null ? session.getQuest().getId() : null)
            .teamId(session.getTeam() != null ? session.getTeam().getId() : null)
            .currentLevelId(session.getCurrentLevelId())
            .maxParticipants(session.getMaxParticipants())
            .participantCount(session.getParticipants() != null ? session.getParticipants().size() : 0)
            .createdAt(session.getCreatedAt())
            .startedAt(session.getStartedAt())
            .finishedAt(session.getFinishedAt())
            .lastActivityAt(session.getLastActivityAt())
            .durationSeconds(session.getDurationSeconds())
            .build();
    }
}