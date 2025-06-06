package cl.metspherical.calbucofelizbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDTO(
    UUID id,
    String username,
    String content,
    LocalDateTime createdAt
) {
}
