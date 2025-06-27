package cl.metspherical.calbucofelizbackend.features.auth.dto;

public record AuthResponseDTO(
    String username,
    TokenDataDTO data
) {
}
