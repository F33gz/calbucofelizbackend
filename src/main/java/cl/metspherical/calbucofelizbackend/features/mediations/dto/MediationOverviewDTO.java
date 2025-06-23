package cl.metspherical.calbucofelizbackend.features.mediations.dto;

import java.util.UUID;

public record MediationOverviewDTO(
        UUID id,
        String title,
        String type,
        String owner_username
) {
}
