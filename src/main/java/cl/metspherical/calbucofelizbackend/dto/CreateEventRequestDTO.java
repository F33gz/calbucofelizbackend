package cl.metspherical.calbucofelizbackend.dto;

public record CreateEventRequestDTO(
        String title,
        String desc,
        String adress,
        String init,
        String ending,
        String username
) {
}
