package cl.metspherical.calbucofelizbackend.features.users.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.users.dto.UserSearchResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnEmptyListWhenSearchTermIsNull() {
        // When
        UserSearchResponseDTO result = userService.searchUsers(null);

        // Then
        assertThat(result.users()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenSearchTermIsEmpty() {
        // When
        UserSearchResponseDTO result = userService.searchUsers("");

        // Then
        assertThat(result.users()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenSearchTermIsWhitespace() {
        // When
        UserSearchResponseDTO result = userService.searchUsers("   ");

        // Then
        assertThat(result.users()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersFound() {
        // Given
        when(userRepository.findUsersWithPostsAndLikesBySearchTerm("nonexistent")).thenReturn(List.of());

        // When
        UserSearchResponseDTO result = userService.searchUsers("nonexistent");

        // Then
        assertThat(result.users()).isEmpty();
    }

    @Test
    void shouldTrimSearchTermAndCallRepository() {
        // Given
        String searchTerm = "  john  ";
        when(userRepository.findUsersWithPostsAndLikesBySearchTerm("john")).thenReturn(List.of());

        // When
        UserSearchResponseDTO result = userService.searchUsers(searchTerm);

        // Then
        assertThat(result.users()).isEmpty();
    }

    @Test
    void shouldReturnUsersWhenFound() {
        // Given
        User user = createMockUser("testuser", "avatar.jpg");
        when(userRepository.findUsersWithPostsAndLikesBySearchTerm("test")).thenReturn(List.of(user));
        when(userRepository.countLikesByUserId(user.getId())).thenReturn(5);

        // When
        UserSearchResponseDTO result = userService.searchUsers("test");

        // Then
        assertThat(result.users()).hasSize(1);
        assertThat(result.users().get(0).username()).isEqualTo("testuser");
        assertThat(result.users().get(0).avatar()).isEqualTo("avatar.jpg");
    }

    private User createMockUser(String username, String avatar) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setAvatar(avatar);
        user.setPosts(Set.of()); // Empty posts
        return user;
    }
}
