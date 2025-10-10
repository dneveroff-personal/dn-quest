package dn.quest.services.impl;

import dn.quest.config.JwtUtil;
import dn.quest.model.dto.LoginRequestDTO;
import dn.quest.model.dto.LoginResponseDTO;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Неправильное имя пользователя или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неправильное имя пользователя или пароль");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }
}
