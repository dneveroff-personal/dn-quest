package dn.quest.controllers;

import dn.quest.config.ApplicationConstants;
import dn.quest.model.dto.LoginRequestDTO;
import dn.quest.model.dto.LoginResponseDTO;
import dn.quest.services.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(Routes.API)
@RequiredArgsConstructor
@Slf4j
public class AuthController implements Routes {

    private final AuthService authService;

    @PostMapping(LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request,
                                   BindingResult bindingResult,
                                   HttpServletRequest httpRequest) {
        // Проверка валидации
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при входе: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Ошибка валидации",
                "errors", bindingResult.getAllErrors()
            ));
        }

        String clientIp = getClientIp(httpRequest);
        
        try {
            LoginResponseDTO response = authService.login(request);
            log.info("Пользователь {} успешно вошел в систему с IP: {}", request.getUsername(), clientIp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Неудачная попытка входа для пользователя {} с IP: {}", request.getUsername(), clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", ApplicationConstants.INVALID_CREDENTIALS
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
