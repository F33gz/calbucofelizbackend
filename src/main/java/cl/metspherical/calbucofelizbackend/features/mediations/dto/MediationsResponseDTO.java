package cl.metspherical.calbucofelizbackend.features.mediations.dto;

import java.util.List;

public record MediationsResponseDTO(
        List<MediationOverviewDTO> mediations
) {
}
