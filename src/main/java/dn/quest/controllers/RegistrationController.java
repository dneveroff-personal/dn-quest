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
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        try {
            UserDTO created = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
