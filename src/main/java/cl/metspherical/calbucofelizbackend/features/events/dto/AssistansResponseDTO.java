package cl.metspherical.calbucofelizbackend.features.events.dto;

public record AssistansResponseDTO(
        String assistanceType,
        UserBasicDTO user
) {
}