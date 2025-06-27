package cl.metspherical.calbucofelizbackend.features.auth.dto;

public record RecoveryRequestDTO(
        String rut,
        String phone
) {
}
