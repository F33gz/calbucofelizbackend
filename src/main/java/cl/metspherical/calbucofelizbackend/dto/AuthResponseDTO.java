package cl.metspherical.calbucofelizbackend.dto;

public record AuthResponseDTO(
    String username,
    TokenDataDTO data
) {
}
