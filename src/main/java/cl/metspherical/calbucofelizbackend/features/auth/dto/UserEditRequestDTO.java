package cl.metspherical.calbucofelizbackend.features.auth.dto;

public record UserEditRequestDTO(
        String username,
        byte[] avatar,
        String description,
        String names,
        String lastNames,
        String password
) {
}
