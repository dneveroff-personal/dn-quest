package dn.quest.services.interfaces;

import dn.quest.model.dto.RegisterDTO;
import dn.quest.model.dto.TeamInvitationDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.UserRole;

import java.util.List;

public interface UserService {

    void delete(Long id);

    UserDTO register(RegisterDTO dto);

    UserDTO getById(Long id);

    UserDTO getByUsername(String username);

    UserDTO getByEmail(String email);

    List<UserDTO> getAll();

    List<UserDTO> getByRole(UserRole role);

    UserDTO updateRole(Long id, UserRole role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<TeamInvitationDTO> getPendingInvitations(Long userId);

}
