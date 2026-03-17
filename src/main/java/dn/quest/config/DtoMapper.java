package dn.quest.config;

import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.TeamMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилитарный класс для маппинга сущностей в DTO и обратно
 */
@Component
public class DtoMapper {

    private final TeamMemberRepository teamMemberRepository;

    public DtoMapper(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    /**
     * Конвертирует User в UserDTO с информацией о команде
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.fromEntity(user, teamMemberRepository);
    }

    /**
     * Конвертирует User в UserDTO без информации о команде
     */
    public UserDTO toUserDTOWithoutTeam(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.fromEntity(user);
    }

    /**
     * Конвертирует список User в список UserDTO
     */
    public List<UserDTO> toUserDTOList(List<User> users) {
        return users.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Конвертирует список User в список UserDTO без информации о командах
     */
    public List<UserDTO> toUserDTOListWithoutTeam(List<User> users) {
        return users.stream()
                .map(this::toUserDTOWithoutTeam)
                .collect(Collectors.toList());
    }

    /**
     * Нормализует строку для сравнения кодов
     */
    public String normalizeCode(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }
}