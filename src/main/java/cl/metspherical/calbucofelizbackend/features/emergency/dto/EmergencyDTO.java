package cl.metspherical.calbucofelizbackend.features.emergency.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmergencyDTO(
        UUID id,
        String username,
        String content,
        LocalDateTime createdAt
) {
}
