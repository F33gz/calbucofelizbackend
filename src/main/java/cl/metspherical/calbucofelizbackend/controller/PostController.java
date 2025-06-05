package cl.metspherical.calbucofelizbackend.controller;

import cl.metspherical.calbucofelizbackend.dto.CreateCommentRequestDTO;
import cl.metspherical.calbucofelizbackend.dto.CreatePostRequestDTO;
import cl.metspherical.calbucofelizbackend.dto.PostCommentsResponseDTO;
import cl.metspherical.calbucofelizbackend.dto.PostDetailDTO;
import cl.metspherical.calbucofelizbackend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, UUID>> createPost(
            @RequestParam("username") String username,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "categoryNames", required = false) Set<String> categoryNames,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {

        List<String> base64Images = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                base64Images.add(Base64.getEncoder().encodeToString(image.getBytes()));
            }
        }

        CreatePostRequestDTO request = new CreatePostRequestDTO(
                username,
                content,
                categoryNames,
                base64Images
        );

        UUID postId = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("postId", postId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailDTO> getPost(@PathVariable UUID id) {
        PostDetailDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getPostImage(@PathVariable UUID imageId) {
        return postService.getPostImageById(imageId)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getContentType()))
                        .body(image.getImg()))
                .orElse(ResponseEntity.notFound().build());
    }    @GetMapping("/{id}/comments")
    public ResponseEntity<PostCommentsResponseDTO> getPostComments(@PathVariable UUID id) {
        PostCommentsResponseDTO comments = postService.getCommentsByPostId(id);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{postId}/comments/create")
    public ResponseEntity<Map<String, UUID>> createComment(
            @PathVariable UUID postId,
            @RequestBody CreateCommentRequestDTO request) {

        UUID commentId = postService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("commentId", commentId));
    }

    @DeleteMapping("/{postId}/comment/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID id) {
        
        postService.deleteComment(postId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
