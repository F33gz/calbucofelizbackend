package cl.metspherical.calbucofelizbackend.features.auth.dto;

public record ProfileResponseDTO(
        String username,
        String avatar,
        String names,
        String lastnames,
        String description,
        String email,
        Integer number
) {
}
