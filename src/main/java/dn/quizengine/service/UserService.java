package dn.quizengine.service;

import dn.quizengine.model.dto.RegisterRequest;
import dn.quizengine.repository.UserRepository;
import dn.quizengine.model.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (!request.getEmail().matches("^[^@]+@[^@]+\\.[^@]+$") ||
                request.getPassword().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
