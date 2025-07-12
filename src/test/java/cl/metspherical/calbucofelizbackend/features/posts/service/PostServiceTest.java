package cl.metspherical.calbucofelizbackend.features.posts.service;

import cl.metspherical.calbucofelizbackend.features.posts.dto.*;
import cl.metspherical.calbucofelizbackend.features.posts.model.Category;
import cl.metspherical.calbucofelizbackend.features.posts.model.Comment;
import cl.metspherical.calbucofelizbackend.features.posts.model.Post;
import cl.metspherical.calbucofelizbackend.features.posts.model.PostImage;
import cl.metspherical.calbucofelizbackend.features.posts.model.PostLike;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.features.posts.repository.CategoryRepository;
import cl.metspherical.calbucofelizbackend.features.posts.repository.CommentRepository;
import cl.metspherical.calbucofelizbackend.features.posts.repository.PostRepository;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.posts.repository.PostLikeRepository;
import cl.metspherical.calbucofelizbackend.common.service.CloudinaryUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private CloudinaryUploadService cloudinaryUploadService;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Post testPost;
    private Category testCategory;
    private Comment testComment;
    private PostLike testPostLike;
    private UUID testUserId;
    private UUID testPostId;
    private UUID testCommentId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPostId = UUID.randomUUID();
        testCommentId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .avatar("avatar.jpg")
                .roles(Set.of("USER"))
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("test-category")
                .build();

        testPost = Post.builder()
                .id(testPostId)
                .content("Test post content")
                .author(testUser)
                .createdAt(LocalDateTime.now())
                .categories(Set.of(testCategory))
                .images(new ArrayList<>())
                .build();

        testComment = Comment.builder()
                .id(testCommentId)
                .content("Test comment")
                .user(testUser)
                .post(testPost)
                .createdAt(LocalDateTime.now())
                .build();

        testPostLike = PostLike.builder()
                .post(testPost)
                .user(testUser)
                .build();
    }

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

    @Test
    void shouldCreatePostSuccessfully() throws IOException {
        // Given
        CreatePostRequestDTO request = new CreatePostRequestDTO(
                testUserId,
                "Test post content",
                Set.of("category1", "category2"),
                List.of(new byte[]{1, 2, 3})
        );

        when(userRepository.getReferenceById(testUserId)).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCase("category1")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCase("category2")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cloudinaryUploadService.uploadImage(any())).thenReturn("https://cloudinary.com/image.jpg");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(testPostId);
            return post;
        });

        // When
        UUID result = postService.createPost(request);

        // Then
        assertThat(result).isEqualTo(testPostId);
        verify(userRepository).getReferenceById(testUserId);
        verify(categoryRepository, times(2)).findByNameIgnoreCase(anyString());
        verify(categoryRepository, times(2)).save(any(Category.class));
        verify(cloudinaryUploadService).uploadImage(any());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void shouldGetPostByIdSuccessfully() {
        // Given
        when(postRepository.findByIdWithDetails(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.countLikesByPostId(testPostId)).thenReturn(5L);
        when(postRepository.countCommentsByPostId(testPostId)).thenReturn(3L);

        // When
        PostDetailDTO result = postService.getPostById(testPostId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testPostId);
        assertThat(result.content()).isEqualTo("Test post content");
        assertThat(result.author().username()).isEqualTo("testuser");
        assertThat(result.likesCount()).isEqualTo(5);
        assertThat(result.commentsCount()).isEqualTo(3);
    }

    @Test
    void shouldCreateCommentSuccessfully() {
        // Given
        CreateCommentRequestDTO request = new CreateCommentRequestDTO(
                testUserId,
                "Test comment content"
        );

        when(postRepository.getReferenceById(testPostId)).thenReturn(testPost);
        when(userRepository.getReferenceById(testUserId)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(testCommentId);
            return comment;
        });

        // When
        UUID result = postService.createComment(testPostId, request);

        // Then
        assertThat(result).isEqualTo(testCommentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldGetCommentsByPostIdSuccessfully() {
        // Given
        List<Comment> comments = List.of(testComment);
        when(postRepository.existsById(testPostId)).thenReturn(true);
        when(commentRepository.findByPostIdWithUser(testPostId)).thenReturn(comments);

        // When
        PostCommentsResponseDTO result = postService.getCommentsByPostId(testPostId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.comments()).hasSize(1);
        assertThat(result.comments().get(0).content()).isEqualTo("Test comment");
        assertThat(result.comments().get(0).username()).isEqualTo("testuser");
    }

    @Test
    void shouldDeleteCommentSuccessfully() {
        // Given
        when(postRepository.existsById(testPostId)).thenReturn(true);
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        // When
        postService.deleteComment(testPostId, testCommentId, testUserId);

        // Then
        verify(commentRepository).delete(testComment);
    }

    @Test
    void shouldThrowExceptionWhenDeletingCommentWithWrongUser() {
        // Given
        UUID wrongUserId = UUID.randomUUID();
        when(postRepository.existsById(testPostId)).thenReturn(true);
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        // When & Then
        assertThatThrownBy(() -> postService.deleteComment(testPostId, testCommentId, wrongUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not the author of this comment");
    }

    @Test
    void shouldDeletePostSuccessfully() {
        // Given
        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));

        // When
        postService.deletePost(testPostId, testUserId);

        // Then
        verify(postRepository).delete(testPost);
    }

    @Test
    void shouldThrowExceptionWhenDeletingPostWithWrongUser() {
        // Given
        UUID wrongUserId = UUID.randomUUID();
        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(testPostId, wrongUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not the author of this post");
    }

    @Test
    void shouldLikePostSuccessfully() {
        // Given
        when(postLikeRepository.existsByPost_IdAndUser_Id(testPostId, testUserId)).thenReturn(false);
        when(postRepository.getReferenceById(testPostId)).thenReturn(testPost);
        when(userRepository.getReferenceById(testUserId)).thenReturn(testUser);
        when(postLikeRepository.save(any(PostLike.class))).thenReturn(testPostLike);

        // When
        postService.likePost(testPostId, testUserId);

        // Then
        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    void shouldThrowExceptionWhenLikingPostAlreadyLiked() {
        // Given
        when(postLikeRepository.existsByPost_IdAndUser_Id(testPostId, testUserId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> postService.likePost(testPostId, testUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User has already liked this post");
    }

    @Test
    void shouldUnlikePostSuccessfully() {
        // Given
        when(postLikeRepository.existsByPost_IdAndUser_Id(testPostId, testUserId)).thenReturn(true);

        // When
        postService.unlikePost(testPostId, testUserId);

        // Then
        verify(postLikeRepository).deleteByPost_IdAndUser_Id(testPostId, testUserId);
    }

    @Test
    void shouldThrowExceptionWhenUnlikingPostNotLiked() {
        // Given
        when(postLikeRepository.existsByPost_IdAndUser_Id(testPostId, testUserId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> postService.unlikePost(testPostId, testUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User has not liked this post");
    }

    @Test
    void shouldGetPostsPaginatedWithoutFilters() {
        // Given
        Page<Post> postsPage = new PageImpl<>(List.of(testPost));
        when(postRepository.findAllPostsWithDetails(any(Pageable.class))).thenReturn(postsPage);
        when(postRepository.countLikesByPostId(testPostId)).thenReturn(5L);
        when(postRepository.countCommentsByPostId(testPostId)).thenReturn(3L);

        // When
        PostPaginatedResponseDTO result = postService.getPostsPaginated(0, 10, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.posts()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }
}
