package cl.metspherical.calbucofelizbackend.features.posts.dto;

import java.util.UUID;

public record CreateCommentRequestDTO(
    String content,
    UUID authorId
) {
}
