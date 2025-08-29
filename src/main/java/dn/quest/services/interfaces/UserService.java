package dn.quest.services.interfaces;

import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.user.User;

import java.util.List;

public interface UserService {

    UserDTO create(UserDTO userDTO);

    UserDTO update(UserDTO userDTO);

    void delete(Long id);

    UserDTO getById(Long id);

    UserDTO getByUsername(String username);

    UserDTO getByEmail(String email);

    List<UserDTO> getAll();
}
