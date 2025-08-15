package dn.quizengine.controller;

import dn.quizengine.model.dto.*;
import dn.quizengine.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/ping")
    public String ping() {
        return "YOU SEND PING - HERE THE PONG 002!";
    }

    @PostMapping(path = "/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("User registered successfully!");
    }

}
