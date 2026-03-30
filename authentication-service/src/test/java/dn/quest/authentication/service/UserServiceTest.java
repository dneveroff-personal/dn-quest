package dn.quest.authentication.service;

import dn.quest.authentication.entity.User;
import dn.quest.authentication.repository.UserPermissionRepository;
import dn.quest.authentication.repository.UserRepository;
import dn.quest.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionRepository userPermissionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("hashedpassword")
                .email("test@example.com")
                .publicName("Test User")
                .role(UserRole.PLAYER)
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void testCreateUser_Success() {
        // Given
        String username = "newuser";
        String password = "password123";
        String email = "newuser@example.com";
        String publicName = "New User";
        UserRole role = UserRole.PLAYER;

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(username, password, email, publicName, role);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(password);
    }

    @Test
    void testCreateUser_UsernameExists_ThrowsException() {
        // Given
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(username, "password", "email@example.com", "Name", UserRole.PLAYER)
        );

        assertEquals("Пользователь с таким именем уже существует", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailExists_ThrowsException() {
        // Given
        String username = "newuser";
        String email = "existing@example.com";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(username, "password", email, "Name", UserRole.PLAYER)
        );

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void testFindByUsername_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testExistsByUsername_True() {
        // Given
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        boolean result = userService.existsByUsername(username);

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void testExistsByUsername_False() {
        // Given
        String username = "nonexistinguser";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        // When
        boolean result = userService.existsByUsername(username);

        // Then
        assertFalse(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(testUser);

        // Then
        assertEquals(testUser, result);
        verify(userRepository).save(testUser);
    }

    @Test
    void testToggleUserStatus_Activate() {
        // Given
        Long userId = 1L;
        User inactiveUser = User.builder()
                .id(userId)
                .username("testuser")
                .isActive(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.toggleUserStatus(userId, true);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testToggleUserStatus_UserNotFound_ThrowsException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.toggleUserStatus(userId, true)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testToDTO_Success() {
        // When
        UserDTO result = userService.toDTO(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getPublicName(), result.getPublicName());
        assertEquals(testUser.getRole(), result.getRole());
        assertEquals(testUser.getIsActive(), result.getIsActive());
        assertEquals(testUser.getIsEmailVerified(), result.getIsEmailVerified());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
        assertEquals(testUser.getLastLoginAt(), result.getLastLoginAt());
    }

    @Test
    void testToDTO_NullInput() {
        // When
        UserDTO result = userService.toDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testUpdateLastLogin_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateLastLogin(username);

        // Then
        verify(userRepository).save(testUser);
        assertNotNull(testUser.getLastLoginAt());
    }

    @Test
    void testUpdateLastLogin_UserNotFound_ThrowsException() {
        // Given
        String username = "nonexistinguser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateLastLogin(username)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}