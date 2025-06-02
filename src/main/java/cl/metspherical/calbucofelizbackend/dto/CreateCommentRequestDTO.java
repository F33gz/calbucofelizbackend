package cl.metspherical.calbucofelizbackend.dto;

public record CreateCommentRequestDTO(
    String content,
    String username
) {
}
