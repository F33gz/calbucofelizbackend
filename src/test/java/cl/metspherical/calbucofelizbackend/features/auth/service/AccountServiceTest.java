package cl.metspherical.calbucofelizbackend.features.auth.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.common.service.CloudinaryUploadService;
import cl.metspherical.calbucofelizbackend.features.auth.dto.ProfileResponseDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.UserEditRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.UserProfileResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryUploadService cloudinaryUploadService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testUpdateUserSuccess() throws IOException {
        UUID userId = UUID.randomUUID();
        UserEditRequestDTO request = new UserEditRequestDTO(
                "newUsername",
                new byte[]{1, 2, 3}, // avatar
                "newDescription", // description
                "newNames",
                "newLastNames",
                "newPassword"
        );
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(cloudinaryUploadService.uploadImage(any(byte[].class))).thenReturn("newAvatarUrl");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponseDTO response = accountService.updateUser(request, userId);

        assertNotNull(response);
        assertEquals("newUsername", response.username());
        assertEquals("newAvatarUrl", response.avatar());
        assertEquals("newDescription", response.description());
        assertEquals("newNames", response.names());
        assertEquals("newLastNames", response.lastNames()); // Corrected to lastNames

        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode("newPassword");
        verify(cloudinaryUploadService).uploadImage(any(byte[].class));
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserPartialData() throws IOException {
        UUID userId = UUID.randomUUID();
        // Request with only username and description. Other fields null.
        UserEditRequestDTO request = new UserEditRequestDTO(
                "onlyUsername",
                null, // avatar
                "onlyDescription", // description
                null, // names
                null, // lastNames
                null  // password
        );

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("oldUsername");
        existingUser.setAvatar("oldAvatarUrl");
        existingUser.setDescription("oldDescription");
        existingUser.setNames("Old Names");
        existingUser.setLastNames("Old LastNames");
        existingUser.setPassword("oldEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        // No password change, so passwordEncoder.encode should not be called for null password
        // No avatar change, so cloudinaryUploadService.uploadImage should not be called
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponseDTO response = accountService.updateUser(request, userId);

        assertNotNull(response);
        assertEquals("onlyUsername", response.username()); // Updated
        assertEquals("oldAvatarUrl", existingUser.getAvatar()); // Unchanged, from original user
        assertEquals("onlyDescription", response.description()); // Updated
        assertEquals("Old Names", response.names()); // Unchanged
        assertEquals("Old LastNames", response.lastNames()); // Unchanged, Corrected to lastNames

        // Verify that existingUser object was modified correctly
        assertEquals("onlyUsername", existingUser.getUsername());
        assertEquals("oldAvatarUrl", existingUser.getAvatar());
        assertEquals("onlyDescription", existingUser.getDescription());
        assertEquals("Old Names", existingUser.getNames());
        assertEquals("Old LastNames", existingUser.getLastNames());
        assertEquals("oldEncodedPassword", existingUser.getPassword()); // Password should not change

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).encode(anyString()); // password was null
        verify(cloudinaryUploadService, never()).uploadImage(any(byte[].class)); // avatar was null
        verify(userRepository).save(existingUser);
    }

    @Test
    void testUpdateUserNotFound() {
        UUID userId = UUID.randomUUID();
        UserEditRequestDTO request = new UserEditRequestDTO(null, null, null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            accountService.updateUser(request, userId);
        });

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserAvatarUploadError() throws IOException {
        UUID userId = UUID.randomUUID();
         UserEditRequestDTO request = new UserEditRequestDTO(
                null, // username
                new byte[]{1, 2, 3}, // avatar
                null, // description
                null, // names
                null, // lastNames
                null // password
        );
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cloudinaryUploadService.uploadImage(any(byte[].class))).thenThrow(new IOException("Upload failed"));

        assertThrows(ResponseStatusException.class, () -> {
            accountService.updateUser(request, userId);
        });

        verify(userRepository).findById(userId);
        verify(cloudinaryUploadService).uploadImage(any(byte[].class));
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void testGetUserProfileSuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setAvatar("avatarUrl");
        user.setNames("Test");
        user.setLastNames("User");
        user.setDescription("Test description");
        user.setEmail("test@example.com");
        user.setNumber(123456789);


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ProfileResponseDTO response = accountService.getUserProfile(userId);

        assertNotNull(response);
        assertEquals("testUser", response.username());
        assertEquals("avatarUrl", response.avatar());
        assertEquals("Test", response.names());
        assertEquals("User", response.lastnames()); // Corrected to lastnames
        assertEquals("Test description", response.description());
        assertEquals("test@example.com", response.email());
        assertEquals(123456789, response.number());

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetUserProfileNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            accountService.getUserProfile(userId);
        });
        verify(userRepository).findById(userId);
    }
}
