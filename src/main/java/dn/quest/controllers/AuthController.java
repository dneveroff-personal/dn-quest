package dn.quest.controllers;

import dn.quest.model.dto.LoginRequestDTO;
import dn.quest.model.dto.LoginResponseDTO;
import dn.quest.services.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Routes.API)
@RequiredArgsConstructor
public class AuthController implements Routes {

    private final AuthService authService;

    @PostMapping(LOGIN)
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
