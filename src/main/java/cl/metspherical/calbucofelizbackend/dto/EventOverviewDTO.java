package cl.metspherical.calbucofelizbackend.dto;

import java.time.LocalDateTime;

public record EventOverviewDTO(
        Integer id,
        String title,
        LocalDateTime init
) {
}
