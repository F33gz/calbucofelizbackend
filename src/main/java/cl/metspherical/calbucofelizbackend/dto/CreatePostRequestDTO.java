package cl.metspherical.calbucofelizbackend.dto;

import java.util.List;
import java.util.Set;

public record CreatePostRequestDTO(
    String username,
    String content,
    Set<String> categoryNames,
    List<String> images
) {
}

