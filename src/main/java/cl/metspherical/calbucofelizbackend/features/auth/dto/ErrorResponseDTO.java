package cl.metspherical.calbucofelizbackend.features.auth.dto;

public record ErrorResponseDTO(
    String status,
    String message
) {
}
