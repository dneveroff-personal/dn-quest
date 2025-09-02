package dn.quest.controllers;

import dn.quest.model.dto.RegisterDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping(Routes.API)
@RequiredArgsConstructor
public class RegistrationController implements Routes {

    private final UserService userService;

    @GetMapping(PING)
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("YOU SEND PING, HERE IS THE PONG TIME -> " + LocalDateTime.now());
    }

    @PostMapping(REGISTER)
    public ResponseEntity<UserDTO> register(@RequestBody RegisterDTO request) {
        // простая валидация
        if (request == null || !StringUtils.hasText(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // проверки уникальности (через сервис)
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (StringUtils.hasText(request.getEmail()) && userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // текущая модель: UserDTO -> publicName; в UserServiceImpl username берётся из publicName
        String publicName = StringUtils.hasText(request.getPublicName())
                ? request.getPublicName()
                : request.getUsername();

        UserDTO dto = UserDTO.builder()
                .publicName(publicName)
                .build();

        UserDTO created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
