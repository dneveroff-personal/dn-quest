package dn.quest.controllers;

import dn.quest.model.dto.UserAdminDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Routes.USERS)
@RequiredArgsConstructor
public class UserController implements Routes {

    private final UserService userService;

    @DeleteMapping(ID)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ID)
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping(USERS_BY_NAME)
    public ResponseEntity<UserDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getByUsername(username));
    }

    @GetMapping(BY_EMAIL)
    public ResponseEntity<UserDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    // ==== Новый универсальный эндпоинт getAll с фильтром по роли ====
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll(@RequestParam(required = false) UserRole role) {
        List<UserDTO> users;

        if (role != null) {
            users = userService.getByRole(role); // фильтр по роли
        } else {
            users = userService.getAll().stream()
                    .map(u -> new UserDTO(u.getId(), u.getPublicName(), u.getRole()))
                    .toList();
        }

        return ResponseEntity.ok(users);
    }

    @GetMapping(ME)
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDTO dto = userService.getByUsername(userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping(ROLE_BY_ID)
    public ResponseEntity<UserDTO> updateRole(@PathVariable Long id, @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.updateRole(id, role));
    }
}
