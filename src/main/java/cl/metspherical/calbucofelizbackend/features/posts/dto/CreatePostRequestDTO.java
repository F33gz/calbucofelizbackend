package cl.metspherical.calbucofelizbackend.features.posts.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreatePostRequestDTO(
    UUID authorId,
    String content,
    Set<String> categoryNames,
    List<byte[]> processedImages
) {
}

