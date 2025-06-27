package cl.metspherical.calbucofelizbackend.features.users.dto;

public record UserSummaryDTO(
        String username,
        String avatar,
        Integer likes,
        Integer post
) {
}
