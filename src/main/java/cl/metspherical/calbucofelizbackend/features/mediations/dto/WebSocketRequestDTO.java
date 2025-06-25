package cl.metspherical.calbucofelizbackend.features.mediations.dto;

import java.util.Map;

public record WebSocketRequestDTO(
        String event,
        Map<String, Object> data
) {
}
