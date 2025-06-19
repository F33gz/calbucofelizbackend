package cl.metspherical.calbucofelizbackend.features.events.dto;

public record CreateEventRequestDTO(
        String title,
        String desc,
        String adress,
        String init,
        String ending,
        String username
) {
}
