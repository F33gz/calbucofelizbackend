package cl.metspherical.calbucofelizbackend.features.events.dto;

import java.time.LocalDateTime;

public record EventOverviewDTO(
        Integer id,
        String title,
        LocalDateTime init
) {
}
