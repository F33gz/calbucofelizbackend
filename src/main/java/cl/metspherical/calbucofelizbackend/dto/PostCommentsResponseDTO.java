package cl.metspherical.calbucofelizbackend.dto;

import java.util.List;

public record PostCommentsResponseDTO(
    List<CommentDTO> comments
) {
}
