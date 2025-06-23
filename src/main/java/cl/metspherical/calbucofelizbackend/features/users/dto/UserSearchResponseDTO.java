package cl.metspherical.calbucofelizbackend.features.users.dto;

import java.util.List;

public record UserSearchResponseDTO(
        List<UserSummaryDTO> users
) {
}
