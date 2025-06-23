package cl.metspherical.calbucofelizbackend.features.posts.dto;

import java.util.List;

public record PostCommentsResponseDTO(
    List<CommentDTO> comments
) {
}
