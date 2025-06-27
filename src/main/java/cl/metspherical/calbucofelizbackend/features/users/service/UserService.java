package cl.metspherical.calbucofelizbackend.features.users.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.users.dto.UserSearchResponseDTO;
import cl.metspherical.calbucofelizbackend.features.users.dto.UserSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Searches for users based on username, names, or lastNames
     * 
     * @param searchTerm The search term to look for in user fields
     * @return UserSearchResponseDTO with list of matching users and their statistics
     */
    public UserSearchResponseDTO searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new UserSearchResponseDTO(List.of()); // Return empty list if no search term
        }

        String normalizedSearchTerm = searchTerm.trim();
        
        // Find users with posts eagerly loaded for statistics calculation
        List<User> users = userRepository.findUsersWithPostsAndLikesBySearchTerm(normalizedSearchTerm);

        // Convert to DTO and limit results
        List<UserSummaryDTO> userSummaries = users.stream()
                .limit(20)
                .map(this::convertToUserSummaryDTO)
                .toList();

        return new UserSearchResponseDTO(userSummaries);
    }

    /**
     * Converts User entity to UserSummaryDTO with post and like counts
     */
    private UserSummaryDTO convertToUserSummaryDTO(User user) {

        // Calculate post count from user's posts relationship
        Integer postCount = user.getPosts().size();
        
        // Count total likes across all user's posts using dedicated query
        Integer totalLikes = userRepository.countLikesByUserId(user.getId());
        
        return new UserSummaryDTO(
                user.getUsername(),
                user.getAvatar(),
                totalLikes != null ? totalLikes : 0,
                postCount
        );
    }
}
