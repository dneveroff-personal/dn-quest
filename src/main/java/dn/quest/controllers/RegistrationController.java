package dn.quest.controllers;

import dn.quest.services.UserServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping(Routes.API)
public class RegistrationController implements Routes {

    private final UserServiceImpl userService;

    public RegistrationController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping(PING)
    public String ping() {
        return "YOU SEND PING, HERE IS THE PONG TIME -> " + LocalDateTime.now();
    }
/*
    @PostMapping(REGISTER)
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("User registered successfully!");
    }*/

}
