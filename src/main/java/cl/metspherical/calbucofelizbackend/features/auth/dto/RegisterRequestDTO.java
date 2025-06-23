package cl.metspherical.calbucofelizbackend.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
    @NotBlank
    String rut,
    @NotBlank
    String names,
    @NotBlank
    String lastnames,
    @NotBlank
    String number,
    @NotBlank
    String password,
    @NotBlank
    String address
) {
} 