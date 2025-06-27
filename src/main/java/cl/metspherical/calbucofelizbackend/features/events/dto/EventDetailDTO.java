package cl.metspherical.calbucofelizbackend.features.events.dto;

import java.time.LocalDateTime;

public record EventDetailDTO(
    Integer id,
    String title,
    String desc,
    String address,
    LocalDateTime init,
    LocalDateTime ending,
    String owner_username
) {
}
