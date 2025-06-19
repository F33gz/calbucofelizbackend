package cl.metspherical.calbucofelizbackend.features.auth.dto;

public record LoginRequestDTO(
    String rut,
    String password
) {
}
