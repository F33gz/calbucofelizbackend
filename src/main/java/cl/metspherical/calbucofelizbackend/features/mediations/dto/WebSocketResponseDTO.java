package cl.metspherical.calbucofelizbackend.features.mediations.dto;

public record WebSocketResponseDTO(
        String event,
        String status,
        Object data
) {
    public WebSocketResponseDTO(String event, String status) {
        this(event, status, null);
    }
}
