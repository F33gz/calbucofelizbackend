package cl.metspherical.calbucofelizbackend.dto;

public record TokenDataDTO(
    String accessToken,
    String refreshToken
) {
}
