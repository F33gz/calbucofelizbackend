package cl.metspherical.calbucofelizbackend.features.mediations.dto;

import java.util.List;

public record CreateMediationRequestDTO(
        String title,
        Boolean type,
        List<String> participants
) {
}
