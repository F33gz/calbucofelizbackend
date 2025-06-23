package cl.metspherical.calbucofelizbackend.features.users.dto;

import java.util.List;

public record UserProfileDTO(
    String username,
    String avatar,
    String description,
    String names,
    String lastNames,
    List<String> roles
) {
}

