package dn.quest.services.impl;

import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDTO create(UserDTO userDTO) {
        User user = toEntity(userDTO);
        user = userRepository.save(user);
        return toDTO(user);
    }

    @Override
    public UserDTO update(UserDTO userDTO) {
        User existing = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        existing.setUsername(userDTO.getPublicName());
        // при необходимости можно добавить другие поля
        return toDTO(userRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getById(Long id) {
        return userRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ===== Маппинг =====
    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .publicName(user.getPublicName())
                .build();
    }

    private User toEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getPublicName());
        return user;
    }
}
