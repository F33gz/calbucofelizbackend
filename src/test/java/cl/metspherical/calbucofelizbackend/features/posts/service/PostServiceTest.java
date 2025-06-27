package cl.metspherical.calbucofelizbackend.features.posts.service;

import cl.metspherical.calbucofelizbackend.features.posts.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        // Given
        UUID postId = UUID.randomUUID();
        when(postRepository.findByIdWithDetails(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void shouldThrowExceptionWhenPostNotFoundForComments() {
        // Given
        UUID postId = UUID.randomUUID();
        when(postRepository.existsById(postId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> postService.getCommentsByPostId(postId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }
}
