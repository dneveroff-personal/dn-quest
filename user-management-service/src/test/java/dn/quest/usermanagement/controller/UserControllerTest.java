package dn.quest.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.enums.UserRole;
import dn.quest.usermanagement.dto.BlockUserRequestDTO;
import dn.quest.usermanagement.dto.UpdateProfileRequestDTO;
import dn.quest.usermanagement.dto.UserProfileDTO;
import dn.quest.usermanagement.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для UserController
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserProfileDTO testUserProfile;
    private UpdateProfileRequestDTO updateProfileRequest;
    private BlockUserRequestDTO blockUserRequest;

    @BeforeEach
    void setUp() {
        testUserProfile = UserProfileDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .publicName("Test User")
                .role(UserRole.PLAYER)
                .avatarUrl("https://example.com/avatar.jpg")
                .bio("Test bio")
                .location("Test location")
                .website("https://example.com")
                .isActive(true)
                .isBlocked(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .lastActivityAt(Instant.now())
                .build();

        updateProfileRequest = UpdateProfileRequestDTO.builder()
                .publicName("Updated Name")
                .email("updated@example.com")
                .avatarUrl("https://example.com/new-avatar.jpg")
                .bio("Updated bio")
                .location("Updated location")
                .website("https://updated-example.com")
                .build();

        blockUserRequest = BlockUserRequestDTO.builder()
                .reason("Test block reason")
                .permanent(false)
                .blockedUntil(Instant.now().plusSeconds(86400))
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_ShouldReturnUserProfile_WhenUserExists() throws Exception {
        when(userProfileService.getUserProfileByUserId(1L)).thenReturn(Optional.of(testUserProfile));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.publicName").value("Test User"))
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        when(userProfileService.getUserProfileByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_ShouldReturnUserProfile_WhenUserExists() throws Exception {
        when(userProfileService.getUserProfileByUsername("testuser")).thenReturn(Optional.of(testUserProfile));

        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserByEmail_ShouldReturnUserProfile_WhenUserExists() throws Exception {
        when(userProfileService.getUserProfileByEmail("test@example.com")).thenReturn(Optional.of(testUserProfile));

        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser", userId = "1")
    void updateUserProfile_ShouldReturnUpdatedProfile_WhenValidRequest() throws Exception {
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .email("updated@example.com")
                .publicName("Updated Name")
                .role(UserRole.PLAYER)
                .isActive(true)
                .isBlocked(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userProfileService.updateUserProfile(eq(1L), any(UpdateProfileRequestDTO.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/users/profile/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.publicName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser", userId = "1")
    void updateUserAvatar_ShouldReturnUpdatedProfile_WhenValidRequest() throws Exception {
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .publicName("Test User")
                .role(UserRole.PLAYER)
                .avatarUrl("https://example.com/new-avatar.jpg")
                .isActive(true)
                .isBlocked(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userProfileService.updateUserAvatar(eq(1L), eq("https://example.com/new-avatar.jpg")))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/users/1/avatar")
                        .with(csrf())
                        .param("avatarUrl", "https://example.com/new-avatar.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/new-avatar.jpg"));
    }

    @Test
    @WithMockUser(username = "testuser", userId = "1")
    void removeUserAvatar_ShouldReturnUpdatedProfile_WhenRequestIsValid() throws Exception {
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .publicName("Test User")
                .role(UserRole.PLAYER)
                .avatarUrl(null)
                .isActive(true)
                .isBlocked(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userProfileService.removeUserAvatar(1L)).thenReturn(updatedProfile);

        mockMvc.perform(delete("/api/users/1/avatar")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.avatarUrl").isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void blockUser_ShouldReturnBlockedProfile_WhenValidRequest() throws Exception {
        UserProfileDTO blockedProfile = UserProfileDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .publicName("Test User")
                .role(UserRole.PLAYER)
                .isActive(true)
                .isBlocked(true)
                .blockReason("Test block reason")
                .blockedUntil(Instant.now().plusSeconds(86400))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userProfileService.blockUser(eq(1L), any(BlockUserRequestDTO.class)))
                .thenReturn(blockedProfile);

        mockMvc.perform(post("/api/users/1/block")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockUserRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isBlocked").value(true))
                .andExpect(jsonPath("$.blockReason").value("Test block reason"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void unblockUser_ShouldReturnUnblockedProfile_WhenRequestIsValid() throws Exception {
        UserProfileDTO unblockedProfile = UserProfileDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .publicName("Test User")
                .role(UserRole.PLAYER)
                .isActive(true)
                .isBlocked(false)
                .blockReason(null)
                .blockedUntil(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userProfileService.unblockUser(1L)).thenReturn(unblockedProfile);

        mockMvc.perform(post("/api/users/1/unblock")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isBlocked").value(false))
                .andExpect(jsonPath("$.blockReason").isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_ShouldReturnNoContent_WhenRequestIsValid() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getBlockedUsers_ShouldReturnListOfBlockedUsers() throws Exception {
        mockMvc.perform(get("/api/users/blocked")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void updateLastActivity_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/users/1/activity")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUserProfile_ShouldReturnForbidden_WhenUserIsNotAdminOrOwner() throws Exception {
        mockMvc.perform(put("/api/users/profile/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isForbidden());
    }
}