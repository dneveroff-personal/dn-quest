package dn.quest.services.interfaces;

import dn.quest.model.dto.LoginRequestDTO;
import dn.quest.model.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}
