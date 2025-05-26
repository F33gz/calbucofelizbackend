package cl.metspherical.calbucofelizbackend.controller;

import cl.metspherical.calbucofelizbackend.dto.CreatePostRequestDTO;
import cl.metspherical.calbucofelizbackend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        CreatePostRequestDTO request = new CreatePostRequestDTO();
        request.setUsername(username);
        request.setContent(content);
        request.setCategoryNames(categoryNames);
        request.setImages(base64Images);

        UUID postId = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("postId", postId));
    }
}
