package cl.metspherical.calbucofelizbackend.dto;

import java.util.List;

public record EventsByMonthResponseDTO(
    String month,
    List<EventOverviewDTO> events
) {
}
