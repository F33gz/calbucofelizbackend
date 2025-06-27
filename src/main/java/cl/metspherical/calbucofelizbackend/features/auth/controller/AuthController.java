package cl.metspherical.calbucofelizbackend.features.auth.controller;

import cl.metspherical.calbucofelizbackend.features.auth.dto.*;
import cl.metspherical.calbucofelizbackend.features.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        Map<String, String> tokens = authenticationService.register(request);

        TokenDataDTO tokenData = new TokenDataDTO(
                tokens.get(ACCESS_TOKEN_KEY),
                tokens.get(REFRESH_TOKEN_KEY)
        );

        String username = tokens.get("username");

        AuthResponseDTO response = new AuthResponseDTO(username,tokenData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Map<String, String> tokens = authenticationService.login(request);

        TokenDataDTO tokenData = new TokenDataDTO(
                tokens.get(ACCESS_TOKEN_KEY),
                tokens.get(REFRESH_TOKEN_KEY)
        );

        String username = tokens.get("username");

        AuthResponseDTO response = new AuthResponseDTO(username, tokenData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        Map<String, String> tokens = authenticationService.refreshToken(request.refreshToken());

        RefreshTokenResponseDTO response = new RefreshTokenResponseDTO(
                tokens.get(ACCESS_TOKEN_KEY)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-recover")
    public ResponseEntity<Void> recovery(@Valid @RequestBody RecoveryRequestDTO request) {
        authenticationService.recoverPassword(request);
        return ResponseEntity.ok().build();
    }
}