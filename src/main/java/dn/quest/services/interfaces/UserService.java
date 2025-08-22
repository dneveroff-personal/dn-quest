package dn.quest.services.interfaces;

import dn.quest.model.dto.UserDTO;

import java.util.List;

public interface UserService {

    // Регистрация: username + email + пароль + publicName
    UserDTO register(String username, String email, String rawPassword, String publicName);

    // Публичное чтение
    UserDTO getById(Integer id);

    // Технические/внутренние сценарии (не отдаём наружу email)
    Integer getIdByUsername(String username); // удобный хелпер для security/поиска

    // Профиль: можно менять publicName и/или email
    UserDTO updateProfile(Integer id, String newPublicName, String newEmail);

    // Смена пароля (по месту — только если понадобится)
    void changePassword(Integer id, String oldRawPassword, String newRawPassword);

    // Служебное
    List<UserDTO> listAll();

}
