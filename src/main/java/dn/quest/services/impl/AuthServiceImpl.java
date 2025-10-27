package dn.quest.services.impl;

import dn.quest.config.ApplicationConstants;
import dn.quest.config.JwtUtil;
import dn.quest.model.dto.LoginRequestDTO;
import dn.quest.model.dto.LoginResponseDTO;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.debug("Попытка входа пользователя: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", request.getUsername());
                    return new IllegalArgumentException(ApplicationConstants.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Неверный пароль для пользователя: {}", request.getUsername());
            throw new IllegalArgumentException(ApplicationConstants.INVALID_CREDENTIALS);
        }

        String token = jwtUtil.generateToken(user.getUsername());
        
        log.info("Пользователь {} успешно аутентифицирован", request.getUsername());
        
        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }
}
