package cl.metspherical.calbucofelizbackend.features.posts.controller;

import cl.metspherical.calbucofelizbackend.features.posts.dto.*;
import cl.metspherical.calbucofelizbackend.features.posts.service.PostService;
import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.common.service.VisionSafeSearchService;
import cl.metspherical.calbucofelizbackend.common.service.ImageCompressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private VisionSafeSearchService visionSafeSearchService;

    @Mock
    private ImageCompressionService imageCompressionService;

    @InjectMocks
    private PostController postController;

    private UUID testUserId;
    private UUID testPostId;
    private UUID testCommentId;
    private AuthorDTO testAuthor;
    private PostDetailDTO testPostDetail;
    private PostCommentsResponseDTO testCommentsResponse;
    private PostPaginatedResponseDTO testPaginatedResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPostId = UUID.randomUUID();
        testCommentId = UUID.randomUUID();

        testAuthor = new AuthorDTO("testuser", "avatar.jpg", Set.of("USER"));

        testPostDetail = new PostDetailDTO(
                testPostId,
                "Test post content",
                LocalDateTime.now(),
                testAuthor,
                List.of("image1.jpg", "image2.jpg"),
                List.of(new CategoryDTO("test-category")),
                5,
                3
        );

        List<CommentDTO> comments = List.of(
                new CommentDTO(testCommentId, "testuser", "Test comment", LocalDateTime.now())
        );
        testCommentsResponse = new PostCommentsResponseDTO(comments);

        testPaginatedResponse = new PostPaginatedResponseDTO(
                List.of(testPostDetail),
                false,
                0,
                true,
                true,
                false
        );
    }

    @Test
    void shouldCreatePostSuccessfully() {
        // Given
        String content = "Test post content";
        Set<String> categoryNames = Set.of("category1", "category2");
        List<MultipartFile> images = List.of(
                new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "image1".getBytes()),
                new MockMultipartFile("image2", "image2.jpg", "image/jpeg", "image2".getBytes())
        );
        List<byte[]> processedImages = List.of("processed1".getBytes(), "processed2".getBytes());

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            when(visionSafeSearchService.validateImages(images)).thenReturn(null);
            when(imageCompressionService.compressImages(images)).thenReturn(processedImages);
            when(postService.createPost(any(CreatePostRequestDTO.class))).thenReturn(testPostId);

            // When
            var response = postController.createPost(content, categoryNames, images);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(201);
            assertThat(response.getBody()).containsEntry("postId", testPostId);
            verify(visionSafeSearchService).validateImages(images);
            verify(imageCompressionService).compressImages(images);
            verify(postService).createPost(any(CreatePostRequestDTO.class));
        }
    }

    @Test
    void shouldGetPostsPaginatedSuccessfully() {
        // Given
        Integer page = 0;
        Integer size = 10;
        String category = "test-category";
        String username = "testuser";

        when(postService.getPostsPaginated(page, size, category, username)).thenReturn(testPaginatedResponse);

        // When
        var response = postController.getPostsPaginated(page, size, category, username);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(testPaginatedResponse);
        verify(postService).getPostsPaginated(page, size, category, username);
    }

    @Test
    void shouldGetPostSuccessfully() {
        // Given
        when(postService.getPostById(testPostId)).thenReturn(testPostDetail);

        // When
        var response = postController.getPost(testPostId);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(testPostDetail);
        verify(postService).getPostById(testPostId);
    }

    @Test
    void shouldGetPostCommentsSuccessfully() {
        // Given
        when(postService.getCommentsByPostId(testPostId)).thenReturn(testCommentsResponse);

        // When
        var response = postController.getPostComments(testPostId);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(testCommentsResponse);
        verify(postService).getCommentsByPostId(testPostId);
    }

    @Test
    void shouldCreateCommentSuccessfully() {
        // Given
        CreateCommentInputDTO input = new CreateCommentInputDTO("Test comment content");

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            when(postService.createComment(eq(testPostId), any(CreateCommentRequestDTO.class))).thenReturn(testCommentId);

            // When
            var response = postController.createComment(testPostId, input);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(201);
            assertThat(response.getBody()).containsEntry("commentId", testCommentId);
            verify(postService).createComment(eq(testPostId), any(CreateCommentRequestDTO.class));
        }
    }

    @Test
    void shouldDeleteCommentSuccessfully() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            doNothing().when(postService).deleteComment(testPostId, testCommentId, testUserId);

            // When
            var response = postController.deleteComment(testPostId, testCommentId);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(204);
            verify(postService).deleteComment(testPostId, testCommentId, testUserId);
        }
    }

    @Test
    void shouldDeletePostSuccessfully() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            doNothing().when(postService).deletePost(testPostId, testUserId);

            // When
            var response = postController.deletePost(testPostId);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(204);
            verify(postService).deletePost(testPostId, testUserId);
        }
    }

    @Test
    void shouldLikePostSuccessfully() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            doNothing().when(postService).likePost(testPostId, testUserId);

            // When
            var response = postController.likePost(testPostId);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).containsEntry("like", true);
            assertThat(response.getBody()).containsEntry("status", "post liked successfully");
            verify(postService).likePost(testPostId, testUserId);
        }
    }

    @Test
    void shouldUnlikePostSuccessfully() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            doNothing().when(postService).unlikePost(testPostId, testUserId);

            // When
            var response = postController.unlikePost(testPostId);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            verify(postService).unlikePost(testPostId, testUserId);
        }
    }
}