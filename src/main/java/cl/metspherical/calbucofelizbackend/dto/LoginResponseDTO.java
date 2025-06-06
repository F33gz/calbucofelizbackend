package cl.metspherical.calbucofelizbackend.dto;

public record LoginResponseDTO(
    String status,
    String username,
    TokenDataDTO data,
    String message
) {
    // Constructor para respuesta exitosa
    public static LoginResponseDTO success(String username, TokenDataDTO data) {
        return new LoginResponseDTO("success", username, data, null);
    }
    
    // Constructor para respuesta de error
    public static LoginResponseDTO error(String message) {
        return new LoginResponseDTO("error", null, null, message);
    }
}
