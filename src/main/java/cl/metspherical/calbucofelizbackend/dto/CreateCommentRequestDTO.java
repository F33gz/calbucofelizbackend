package cl.metspherical.calbucofelizbackend.dto;

import java.util.UUID;

public record CreateCommentRequestDTO(
    String content,
    UUID authorId
) {
}
