package dn.quest.usermanagement.controller;

import dn.quest.shared.dto.PaginationDTO;
import dn.quest.usermanagement.dto.BlockUserRequestDTO;
import dn.quest.usermanagement.dto.UpdateProfileRequestDTO;
import dn.quest.usermanagement.dto.UserProfileDTO;
import dn.quest.usermanagement.dto.UserSearchRequestDTO;
import dn.quest.usermanagement.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления профилями пользователей
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API для управления профилями пользователей")
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/{id}")
    @Operation(summary = "Получить профиль пользователя по ID", description = "Возвращает профиль пользователя по указанному ID")
    public ResponseEntity<UserProfileDTO> getUserById(
            @Parameter(description = "ID пользователя") @PathVariable Long id) {
        
        return userProfileService.getUserProfileByUserId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Получить профиль пользователя по userId", description = "Возвращает профиль пользователя по userId из Authentication Service")
    public ResponseEntity<UserProfileDTO> getUserProfileByUserId(
            @Parameter(description = "ID пользователя из Authentication Service") @PathVariable Long userId) {
        
        return userProfileService.getUserProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Получить профиль пользователя по имени", description = "Возвращает профиль пользователя по имени пользователя")
    public ResponseEntity<UserProfileDTO> getUserByUsername(
            @Parameter(description = "Имя пользователя") @PathVariable String username) {
        
        return userProfileService.getUserProfileByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Получить профиль пользователя по email", description = "Возвращает профиль пользователя по email")
    public ResponseEntity<UserProfileDTO> getUserByEmail(
            @Parameter(description = "Email пользователя") @PathVariable String email) {
        
        return userProfileService.getUserProfileByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Получить список пользователей", description = "Возвращает список пользователей с пагинацией и фильтрацией")
    public ResponseEntity<PaginationDTO<UserProfileDTO>> getAllUsers(
            @Parameter(description = "Параметры поиска и пагинации") @ModelAttribute UserSearchRequestDTO request) {
        
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<UserProfileDTO> users = userProfileService.searchUsers(request, pageable);
        
        PaginationDTO<UserProfileDTO> response = PaginationDTO.<UserProfileDTO>builder()
                .content(users.getContent())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить профиль пользователя", description = "Обновляет профиль пользователя")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        
        UserProfileDTO updatedProfile = userProfileService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/{userId}/avatar")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить аватар пользователя", description = "Обновляет аватар пользователя")
    public ResponseEntity<UserProfileDTO> updateUserAvatar(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "URL аватара") @RequestParam String avatarUrl) {
        
        UserProfileDTO updatedProfile = userProfileService.updateUserAvatar(userId, avatarUrl);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{userId}/avatar")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Удалить аватар пользователя", description = "Удаляет аватар пользователя")
    public ResponseEntity<UserProfileDTO> removeUserAvatar(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        UserProfileDTO updatedProfile = userProfileService.removeUserAvatar(userId);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать пользователя", description = "Блокирует пользователя")
    public ResponseEntity<UserProfileDTO> blockUser(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Valid @RequestBody BlockUserRequestDTO request) {
        
        UserProfileDTO blockedProfile = userProfileService.blockUser(userId, request);
        return ResponseEntity.ok(blockedProfile);
    }

    @PostMapping("/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Разблокировать пользователя", description = "Разблокирует пользователя")
    public ResponseEntity<UserProfileDTO> unblockUser(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        UserProfileDTO unblockedProfile = userProfileService.unblockUser(userId);
        return ResponseEntity.ok(unblockedProfile);
    }

    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать пользователя", description = "Активирует пользователя")
    public ResponseEntity<UserProfileDTO> activateUser(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        UserProfileDTO activatedProfile = userProfileService.activateUser(userId);
        return ResponseEntity.ok(activatedProfile);
    }

    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Деактивировать пользователя", description = "Деактивирует пользователя")
    public ResponseEntity<UserProfileDTO> deactivateUser(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        UserProfileDTO deactivatedProfile = userProfileService.deactivateUser(userId);
        return ResponseEntity.ok(deactivatedProfile);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить профиль пользователя", description = "Удаляет профиль пользователя")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        userProfileService.deleteUserProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blocked")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить заблокированных пользователей", description = "Возвращает список заблокированных пользователей")
    public ResponseEntity<List<UserProfileDTO>> getBlockedUsers() {
        List<UserProfileDTO> blockedUsers = userProfileService.getBlockedUsers();
        return ResponseEntity.ok(blockedUsers);
    }

    @GetMapping("/recent/{days}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить недавно зарегистрированных пользователей", description = "Возвращает список пользователей, зарегистрированных за последние N дней")
    public ResponseEntity<List<UserProfileDTO>> getRecentlyRegisteredUsers(
            @Parameter(description = "Количество дней") @PathVariable Integer days) {
        
        List<UserProfileDTO> recentUsers = userProfileService.getRecentlyRegisteredUsers(days);
        return ResponseEntity.ok(recentUsers);
    }

    @GetMapping("/statistics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить сводную статистику пользователей", description = "Возвращает сводную статистику по пользователям")
    public ResponseEntity<dn.quest.usermanagement.dto.UserStatisticsSummaryDTO> getUserStatisticsSummary() {
        dn.quest.usermanagement.dto.UserStatisticsSummaryDTO summary = userProfileService.getUserStatisticsSummary();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{userId}/activity")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить активность пользователя", description = "Обновляет время последней активности пользователя")
    public ResponseEntity<Void> updateLastActivity(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        userProfileService.updateLastActivity(userId);
        return ResponseEntity.ok().build();
    }
}