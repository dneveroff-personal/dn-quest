package dn.quest.services;

import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.UserService;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // бин уже есть в проекте

    @Override
    public UserDTO register(String username, String email, String rawPassword, String publicName) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("username already taken");
        });
        // если email обязателен, стоит проверять на null/blank и уникальность
        if (email != null && !email.isBlank()) {
            // у тебя на таблице стоит unique constraint по email
            userRepository.findAll().stream()
                    .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                    .findAny()
                    .ifPresent(u -> { throw new IllegalArgumentException("email already taken"); });
        }

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setPublicName(publicName);
        u.getRoles().add(UserRole.PLAYER);

        u = userRepository.save(u);
        return toDto(u);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Integer id) {
        return userRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    @Override
    public UserDTO updateProfile(Integer id, String newPublicName, String newEmail) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        if (newPublicName != null) u.setPublicName(newPublicName);

        if (newEmail != null) {
            // простая проверка уникальности; можно вынести в репозиторий
            boolean emailTaken = userRepository.findAll().stream()
                    .anyMatch(other -> !other.getId().equals(id)
                            && newEmail.equalsIgnoreCase(other.getEmail()));
            if (emailTaken) throw new IllegalArgumentException("email already taken");

            u.setEmail(newEmail);
        }

        return toDto(userRepository.save(u));
    }

    @Override
    public void changePassword(Integer id, String oldRawPassword, String newRawPassword) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        if (!passwordEncoder.matches(oldRawPassword, u.getPasswordHash())) {
            throw new IllegalArgumentException("old password invalid");
        }
        u.setPasswordHash(passwordEncoder.encode(newRawPassword));
        userRepository.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    private UserDTO toDto(User u) {
        return new UserDTO(u.getId(), u.getPublicName());
    }
}
