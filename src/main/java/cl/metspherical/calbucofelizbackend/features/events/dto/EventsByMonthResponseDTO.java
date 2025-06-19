package cl.metspherical.calbucofelizbackend.features.events.dto;

import java.util.List;

public record EventsByMonthResponseDTO(
    String month,
    List<EventOverviewDTO> events
) {
}
