package cl.metspherical.calbucofelizbackend.features.auth.service;

import cl.metspherical.calbucofelizbackend.features.auth.dto.ProfileResponseDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.UserEditRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.UserProfileResponseDTO;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.common.service.CloudinaryUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final CloudinaryUploadService cloudinaryUploadService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Updates user account information
     * Only updates fields that are not null in the request
     *
     * @param request DTO containing user edit data
     * @return UserProfileResponseDTO with updated user data
     */
    @Transactional
    public UserProfileResponseDTO updateUser(UserEditRequestDTO request,UUID currentUserId) {
        // Find user in database
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update text fields using functional approach
        updateIfPresent(request.username(), user::setUsername);
        updateIfPresent(request.description(), user::setDescription);
        updateIfPresent(request.names(), user::setNames);
        updateIfPresent(request.lastNames(), user::setLastNames);
        
        // Update password with encoding
        updateIfPresent(request.password(), password -> 
            user.setPassword(passwordEncoder.encode(password)));

        // Update avatar if provided
        updateAvatar(request.avatar(), user);

        // Save updated user
        User savedUser = userRepository.save(user);
        
        // Return updated user data
        return mapToUserProfileResponse(savedUser);
    }

    /**
     * Retrieves the user profile by user ID.
     * Throws an exception if the user is not found.
     *
     * @param userId UUID of the user whose profile is to be retrieved
     * @return ProfileResponseDTO containing the user's profile data
     * @throws ResponseStatusException if the user is not found
     */
    public ProfileResponseDTO getUserProfile (UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));

        return new ProfileResponseDTO(
                user.getUsername(),
                user.getAvatar(),
                user.getNames(),
                user.getLastNames(),
                user.getDescription(),
                user.getEmail(),
                user.getNumber()
        );
    }

    /**
     * Updates a field if the value is present and not empty
     */
    private void updateIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.trim().isEmpty()) {
            setter.accept(value.trim());
        }
    }

    /**
     * Updates user avatar by uploading to Cloudinary
     */
    private void updateAvatar(byte[] avatar, User user) {
        if (avatar != null && avatar.length > 0) {
            try {
                String avatarUrl = cloudinaryUploadService.uploadImage(avatar);
                user.setAvatar(avatarUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error uploading avatar: " + e.getMessage());
            }
        }
    }

    /**
     * Maps User entity to UserProfileResponseDTO
     */
    private UserProfileResponseDTO mapToUserProfileResponse(User user) {
        return new UserProfileResponseDTO(
                user.getUsername(),
                user.getAvatar(),
                user.getDescription(),
                user.getNames(),
                user.getLastNames(),
                user.getRoles()
        );
    }
}
