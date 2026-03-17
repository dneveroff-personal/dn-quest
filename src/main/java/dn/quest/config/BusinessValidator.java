package dn.quest.config;

import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Утилитарный класс для валидации бизнес-правил
 */
@Slf4j
@Component
public class BusinessValidator {

    /**
     * Проверяет, может ли пользователь редактировать квест
     */
    public boolean canEditQuest(User user, Quest quest) {
        if (user == null || quest == null) {
            return false;
        }
        
        // Админ может редактировать любой квест
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }
        
        // Автор может редактировать свой квест
        if (user.getRole() == UserRole.AUTHOR && quest.getAuthors().contains(user)) {
            return true;
        }
        
        return false;
    }

    /**
     * Проверяет, может ли пользователь редактировать квест (используя UserDTO)
     */
    public boolean canEditQuest(UserDTO userDTO, Quest quest) {
        if (userDTO == null || quest == null) {
            return false;
        }
        
        // Админ может редактировать любой квест
        if (userDTO.getRole() == UserRole.ADMIN) {
            return true;
        }
        
        // Автор может редактировать свой квест
        if (userDTO.getRole() == UserRole.AUTHOR) {
            return quest.getAuthors().stream()
                    .anyMatch(author -> author.getId().equals(userDTO.getId()));
        }
        
        return false;
    }

    /**
     * Проверяет, начался ли квест
     */
    public boolean isQuestStarted(Quest quest) {
        if (quest == null || quest.getStartAt() == null) {
            return true; // Если дата начала не указана, считаем что квест начался
        }
        
        return Instant.now().isAfter(quest.getStartAt()) || Instant.now().equals(quest.getStartAt());
    }

    /**
     * Проверяет, закончился ли квест
     */
    public boolean isQuestFinished(Quest quest) {
        if (quest == null || quest.getEndAt() == null) {
            return false; // Если дата окончания не указана, квест не закончился
        }
        
        return Instant.now().isAfter(quest.getEndAt());
    }

    /**
     * Проверяет, активен ли квест (начался и не закончился)
     */
    public boolean isQuestActive(Quest quest) {
        return isQuestStarted(quest) && !isQuestFinished(quest);
    }

    /**
     * Проверяет, может ли пользователь создавать квесты
     */
    public boolean canCreateQuest(User user) {
        return user != null && (user.getRole() == UserRole.AUTHOR || user.getRole() == UserRole.ADMIN);
    }

    /**
     * Проверяет, может ли пользователь создавать квесты (используя UserDTO)
     */
    public boolean canCreateQuest(UserDTO userDTO) {
        return userDTO != null && (userDTO.getRole() == UserRole.AUTHOR || userDTO.getRole() == UserRole.ADMIN);
    }

    /**
     * Проверяет, является ли пользователь капитаном команды
     */
    public boolean isTeamCaptain(UserDTO userDTO) {
        return userDTO != null && userDTO.isCaptain();
    }

    /**
     * Проверяет, может ли пользователь управлять пользователями
     */
    public boolean canManageUsers(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    /**
     * Проверяет, может ли пользователь управлять пользователями (используя UserDTO)
     */
    public boolean canManageUsers(UserDTO userDTO) {
        return userDTO != null && userDTO.getRole() == UserRole.ADMIN;
    }

    /**
     * Проверяет, является ли строка валидным кодом (не пустая и не только пробелы)
     */
    public boolean isValidCode(String code) {
        return code != null && !code.trim().isEmpty();
    }

    /**
     * Проверяет, является ли email валидным
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email не обязательный
        }
        
        return email.matches(ApplicationConstants.EMAIL_REGEX);
    }

    /**
     * Проверяет, является ли имя пользователя валидным
     */
    public boolean isValidUsername(String username) {
        return username != null &&
               !username.trim().isEmpty() &&
               username.length() >= ApplicationConstants.MIN_USERNAME_LENGTH &&
               username.length() <= ApplicationConstants.MAX_USERNAME_LENGTH &&
               username.matches(ApplicationConstants.USERNAME_REGEX);
    }

    /**
     * Проверяет, является ли пароль валидным
     */
    public boolean isValidPassword(String password) {
        return password != null &&
               !password.trim().isEmpty() &&
               password.length() >= ApplicationConstants.MIN_PASSWORD_LENGTH &&
               password.length() <= ApplicationConstants.MAX_PASSWORD_LENGTH;
    }
}