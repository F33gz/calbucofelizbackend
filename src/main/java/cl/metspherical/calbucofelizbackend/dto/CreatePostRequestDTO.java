package cl.metspherical.calbucofelizbackend.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreatePostRequestDTO(
    UUID authorId,
    String content,
    Set<String> categoryNames,
    List<String> images
) {
}

