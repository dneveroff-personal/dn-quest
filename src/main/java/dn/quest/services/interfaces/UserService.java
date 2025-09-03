package dn.quest.services.interfaces;

import dn.quest.model.dto.RegisterDTO;
import dn.quest.model.dto.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO update(UserDTO userDTO);

    void delete(Long id);

    UserDTO register(RegisterDTO dto);

    UserDTO getById(Long id);

    UserDTO getByUsername(String username);

    UserDTO getByEmail(String email);

    List<UserDTO> getAll();

    // ---- добавлено для регистрации ----
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
