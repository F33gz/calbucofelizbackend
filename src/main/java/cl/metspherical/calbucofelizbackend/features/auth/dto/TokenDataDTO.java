package cl.metspherical.calbucofelizbackend.features.auth.dto;

public record TokenDataDTO(
    String accessToken,
    String refreshToken
) {
}
