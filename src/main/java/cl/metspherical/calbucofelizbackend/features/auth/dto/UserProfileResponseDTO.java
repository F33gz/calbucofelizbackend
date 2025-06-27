package cl.metspherical.calbucofelizbackend.features.auth.dto;

import java.util.List;

public record UserProfileResponseDTO(
        String username,
        String avatar,
        String description,
        String names,
        String lastNames,
        List<String> roles
) {
}
