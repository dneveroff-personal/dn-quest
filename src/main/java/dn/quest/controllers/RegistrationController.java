package dn.quest.controllers;

import dn.quest.config.ApplicationConstants;
import dn.quest.model.dto.RegisterDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.services.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(Routes.API)
@RequiredArgsConstructor
@Slf4j
public class RegistrationController implements Routes {

    private final UserService userService;

    @GetMapping(PING)
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("YOU SEND PING, HERE IS THE PONG TIME -> " + LocalDateTime.now());
    }

    @PostMapping(REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO request,
                                      BindingResult bindingResult,
                                      HttpServletRequest httpRequest) {
        // Проверка валидации
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при регистрации: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Ошибка валидации",
                "errors", bindingResult.getAllErrors()
            ));
        }

        String clientIp = getClientIp(httpRequest);
        
        try {
            UserDTO created = userService.register(request);
            log.info("Пользователь {} успешно зарегистрирован с IP: {}", request.getUsername(), clientIp);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.warn("Ошибка при регистрации пользователя {} с IP: {}: {}",
                    request.getUsername(), clientIp, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", e.getMessage()
            ));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
