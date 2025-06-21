package cl.metspherical.calbucofelizbackend.features.posts.service;

import cl.metspherical.calbucofelizbackend.features.posts.dto.CreatePostRequestDTO;
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
import cl.metspherical.calbucofelizbackend.features.posts.repository.PostImageRepository;
import cl.metspherical.calbucofelizbackend.features.posts.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostImageRepository postImageRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    /**
     * Creates a new post in the system
     *
     * @param request DTO containing post creation data
     * @return UUID of the created post
     */
    @Transactional
    public UUID createPost(CreatePostRequestDTO request) {
        // 1. Validate and get user
        User author = userRepository.getReferenceById(request.authorId());

        // 2. Create base post
        Post post = Post.builder()
                .content(sanitizeContent(request.content()))
                .author(author)
                .build();

        // 3. Process categories with find-or-create logic
        if (request.categoryNames() != null && !request.categoryNames().isEmpty()) {
            Set<Category> categories = processCategories(request.categoryNames());
            categories.forEach(post::addCategory);
        }

        // 4. Process images (directly as byte[])
        if (request.images() != null && !request.images().isEmpty()) {
            processImages(request.images(), post);
        }

        // 5. Save and return ID
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    /**
     * Processes categories, creating new ones if they don't exist
     *
     * @param categoryNames Set of category names to process
     * @return Set of Category objects
     */
    private Set<Category> processCategories(Set<String> categoryNames) {
        Set<Category> categories = new HashSet<>();

        for (String name : categoryNames) {
            String normalizedName = name.trim().toLowerCase();

            // Look for existing category
            Category category = categoryRepository.findByNameIgnoreCase(normalizedName)
                    .orElseGet(() -> {
                        // Create new category if it doesn't exist
                        Category newCategory = Category.builder()
                                .name(normalizedName)
                                .build();
                        return categoryRepository.save(newCategory);
                    });

            categories.add(category);
        }

        return categories;
    }

    /**
     * Processes base64 encoded images and associates them with a post
     *
     * @param base64ImageList List of base64 encoded images
     * @param post Post to associate images with
     */
    private void processImages(List<String> base64ImageList, Post post) {
        if (base64ImageList.size() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 10 images allowed");
        }

        for (String base64Image : base64ImageList) {
            try {
                byte[] decodedImageBytes = decodeBase64Image(base64Image);

                PostImage postImage = PostImage.builder()
                        .img(decodedImageBytes)
                        .contentType(detectContentType(decodedImageBytes))
                        .build();

                post.addImage(postImage);

            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing image: " + e.getMessage());
            }
        }
    }

    /**
     * Sanitizes post content
     *
     * @param content Content to sanitize
     * @return Sanitized content or null if empty
     */
    private String sanitizeContent(String content) {
        return content != null && !content.trim().isEmpty() ? content.trim() : null;
    }

    /**
     * Decodes a base64 encoded image
     *
     * @param base64Image Base64 encoded image string
     * @return Decoded image as byte array
     */
    private byte[] decodeBase64Image(String base64Image) {
        String cleanBase64 = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");
        return Base64.getDecoder().decode(cleanBase64);
    }

    /**
     * Detects the content type of an image based on its bytes
     *
     * @param imageBytes Image bytes to analyze
     * @return MIME type of the image
     */
    private String detectContentType(byte[] imageBytes) {
        if (imageBytes.length >= 4) {
            if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
                return "image/jpeg";
            }
            if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == 0x50 &&
                    imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
                return "image/png";
            }
            if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46) {
                return "image/gif";
            }
        }
        return "image/jpeg";
    }

    /**
     * Gets a post by its ID with all related details
     *
     * @param id ID of the post to retrieve
     * @return PostDetailDTO containing post information
     */
    public PostDetailDTO getPostById(UUID id) {
        Post post = postRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return mapPostToPostDetailDTO(post);
    }

    /**
     * Gets a post image by its ID
     *
     * @param imageId ID of the image to retrieve
     * @return Optional containing the PostImage if found
     */
    public Optional<PostImage> getPostImageById(UUID imageId) {
        return postImageRepository.findById(imageId);
    }

    /**
     * Maps a Post entity to its DTO representation
     *
     * @param post Post entity to convert
     * @return PostDetailDTO with post information
     */
    private PostDetailDTO mapPostToPostDetailDTO(Post post) {
        AuthorDTO authorDTO = new AuthorDTO(
                post.getAuthor().getUsername(),
                post.getAuthor().getAvatar(),
                post.getAuthor().getRoles()
        );

        List<String> images = post.getImages().stream()
                .map(image ->
                        buildImageUrl(image.getId()))
                .toList();

        List<CategoryDTO> categoryDTOs = post.getCategories().stream()
                .map(category -> new CategoryDTO(
                        category.getName()))
                .toList();

        // Use COUNT queries to avoid ConcurrentModificationException
        long likesCount = postRepository.countLikesByPostId(post.getId());
        long commentsCount = postRepository.countCommentsByPostId(post.getId());

        return new PostDetailDTO(
                post.getId(),
                post.getContent(),
                post.getCreatedAt(),
                authorDTO,
                images,
                categoryDTOs,
                (int) likesCount,
                (int) commentsCount
        );
    }

    /**
     * Builds a URL for accessing a post image
     *
     * @param imageId ID of the image
     * @return Complete URL to access the image
     */
    private String buildImageUrl(UUID imageId) {
        return "http://localhost:8080/api/posts/image/" + imageId.toString();
    }

    /**
     * Creates a new comment for a post
     *
     * @param postId ID of the post to comment on
     * @param request DTO containing comment creation data
     * @return UUID of the created comment
     */
    @Transactional
    public UUID createComment(UUID postId, CreateCommentRequestDTO request) {
        // 1. Validate and get post
        Post post = postRepository.getReferenceById(postId);
        User user = userRepository.getReferenceById(request.authorId());

        // 2. Create comment
        Comment comment = Comment.builder()
                .content(sanitizeContent(request.content()))
                .post(post)
                .user(user)
                .build();

        // 3. Save and return ID
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    /**
     * Gets all comments for a specific post
     *
     * @param postId ID of the post to get comments for
     * @return PostCommentsResponseDTO containing comment information
     */
    public PostCommentsResponseDTO getCommentsByPostId(UUID postId) {
        // 1. Validate post exists
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        // 2. Get comments directly with optimized query
        List<CommentDTO> comments = commentRepository.findByPostIdWithUser(postId).stream()
                .map(comment -> new CommentDTO(
                        comment.getId(),
                        comment.getUser().getUsername(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .toList();

        // 3. Return wrapped in response DTO
        return new PostCommentsResponseDTO(comments);
    }
    /**
     * Deletes a comment from a post
     *
     * @param postId ID of the post containing the comment
     * @param commentId ID of the comment to delete
     * @throws RuntimeException if post or comment not found, or comment doesn't belong to the post
     */
    @Transactional
    public void deleteComment(UUID postId, UUID commentId, UUID userId) {
        // 1. Validate post exists
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        // 2. Validate comment exists
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        // 3. Validate user is the author of the comment
        if (!comment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not the author of this comment");
        }
                
        // 4. Validate comment belongs to the post
        if (!comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment does not belong to the specified post");
        }

        // 5. Delete the comment
        commentRepository.delete(comment);
    }

    /**
     * Deletes a post and all its associated data
     *
     * @param postId ID of the post to delete
     * @throws RuntimeException if post not found
     */
    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        // 1. Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 2. Validate user is the author of the post
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not the author of this post");
        }
        // 3. Delete the post (cascade will handle comments, images, and category relationships)
        postRepository.delete(post);
    }
    /**
     * Adds a like to a post
     *
     * @param postId ID of the post to like
     * @param userId ID of the user liking the post
     * @throws RuntimeException if post or user not found, or if user already liked the post
     */
    @Transactional
    public void likePost(UUID postId, UUID userId) {
        // 1. Check if user already liked the post
        if (postLikeRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has already liked this post");
        }

        // 2. Use lazy references
        Post post = postRepository.getReferenceById(postId);
        User user = userRepository.getReferenceById(userId);

        // 3. Create and save the like
        PostLike postLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();

        postLikeRepository.save(postLike);
    }
    /**
     * Removes a like from a post
     *
     * @param postId ID of the post to unlike
     * @param userId ID of the user unliking the post
     * @throws RuntimeException if post or user not found, or if user hasn't liked the post
     */
    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        // 1. Check if user has liked the post
        if (!postLikeRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has not liked this post");
        }

        // 2. Delete the like directly
        postLikeRepository.deleteByPost_IdAndUser_Id(postId, userId);
    }
}
