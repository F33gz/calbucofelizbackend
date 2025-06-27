package cl.metspherical.calbucofelizbackend.features.posts.controller;

import cl.metspherical.calbucofelizbackend.features.posts.dto.*;
import cl.metspherical.calbucofelizbackend.features.posts.service.PostService;
import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.common.service.VisionSafeSearchService;
import cl.metspherical.calbucofelizbackend.common.service.ImageCompressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final VisionSafeSearchService visionSafeSearchService;
    private final ImageCompressionService imageCompressionService;    

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, UUID>> createPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "categoryNames", required = false) Set<String> categoryNames,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        UUID authorId = SecurityUtils.getCurrentUserId();

        visionSafeSearchService.validateImages(images);

        List<byte[]> processedImages = imageCompressionService.compressImages(images);

        CreatePostRequestDTO request = new CreatePostRequestDTO(
                authorId,
                content,
                categoryNames,
                processedImages
        );

        UUID postId = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("postId", postId));
    }

    @GetMapping()
    public ResponseEntity<PostPaginatedResponseDTO> getPostsPaginated(
            @RequestParam Integer page, 
            @RequestParam Integer size,
            @RequestParam(required = false) String category, 
            @RequestParam(required = false) String username) {
        
        PostPaginatedResponseDTO response = postService.getPostsPaginated(page, size, category, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailDTO> getPost(@PathVariable UUID id) {
        PostDetailDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<PostCommentsResponseDTO> getPostComments(@PathVariable UUID id) {
        PostCommentsResponseDTO comments = postService.getCommentsByPostId(id);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{postId}/comments/create")
    public ResponseEntity<Map<String, UUID>> createComment(
            @PathVariable UUID postId,
            @RequestBody CreateCommentInputDTO input) {

        UUID authorId = SecurityUtils.getCurrentUserId();
        CreateCommentRequestDTO request = new CreateCommentRequestDTO(
            input.content(),
            authorId
        );

        UUID commentId = postService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("commentId", commentId));
    }

    @DeleteMapping("/{postId}/comment/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();

        postService.deleteComment(postId, id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable UUID postId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        postService.likePost(postId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("like", true);
        response.put("status", "post liked successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable UUID postId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        postService.unlikePost(postId, userId);
        return ResponseEntity.ok().build();
    }
}
