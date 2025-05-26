package cl.metspherical.calbucofelizbackend.service;

import cl.metspherical.calbucofelizbackend.dto.CreatePostRequestDTO;
import cl.metspherical.calbucofelizbackend.model.Category;
import cl.metspherical.calbucofelizbackend.model.Post;
import cl.metspherical.calbucofelizbackend.model.PostImage;
import cl.metspherical.calbucofelizbackend.model.User;
import cl.metspherical.calbucofelizbackend.repository.CategoryRepository;
import cl.metspherical.calbucofelizbackend.repository.PostRepository;
import cl.metspherical.calbucofelizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public UUID createPost(CreatePostRequestDTO request) {
        // 1. validar y obtener usuario
        User author = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. crear post base
        Post post = Post.builder()
                .content(sanitizeContent(request.getContent()))
                .author(author)
                .build();

        // 3. procesar categorías con lógica find-or-create
        if (request.getCategoryNames() != null && !request.getCategoryNames().isEmpty()) {
            Set<Category> categories = processCategories(request.getCategoryNames());
            categories.forEach(post::addCategory);
        }

        // 4. procesar imágenes (ahora directamente como byte[])
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            processImages(request.getImages(), post);
        }

        // 5. guardar y retornar ID
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    private Set<Category> processCategories(Set<String> categoryNames) {
        Set<Category> categories = new HashSet<>();

        for (String name : categoryNames) {
            String normalizedName = name.trim().toLowerCase();

            // buscar categoría existente
            Category category = categoryRepository.findByNameIgnoreCase(normalizedName)
                    .orElseGet(() -> {
                        // crear nueva categoría si no existe
                        Category newCategory = Category.builder()
                                .name(normalizedName)
                                .build();
                        return categoryRepository.save(newCategory);
                    });

            categories.add(category);
        }

        return categories;
    }

    private void processImages(List<String> base64ImageList, Post post) {
        if (base64ImageList.size() > 10) {
            throw new RuntimeException("Maximum 10 images allowed");
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
                throw new RuntimeException("Error processing image: " + e.getMessage());
            }
        }
    }

    private String sanitizeContent(String content) {
        return content != null && !content.trim().isEmpty() ? content.trim() : null;
    }

    private byte[] decodeBase64Image(String base64Image) {
        String cleanBase64 = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");
        return Base64.getDecoder().decode(cleanBase64);
    }

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

}
